package ass.prochka6;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;

import ass.prochka6.cache.Cache;
import ass.prochka6.cache.CacheManager;
import ass.prochka6.cache.Element;

/**
 * Http server file resource handler.
 *
 * @author Kamil Prochazka
 */
class FileResourceRequestHandler implements RequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger(FileResourceRequestHandler.class);

    private static final String HTACCESS_FILE_NAME = ".htaccess";
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    private static final MimetypesFileTypeMap MIME_MAP = new MimetypesFileTypeMap();

    /**
     * Max cached data size of 50MB
     */
    private static final long MAX_CACHED_DATA_SIZE = 50 * 1000 * 1024;

    private Cache authenticationCache = new Cache(300, 300);
    // Max number of elements 200 with total size of 1GB, living for at max 30mins with 10mins to live in cache
    private Cache dataCache = new Cache(10 * 60, 30 * 60, 200, 1000 * 1000 * 1024);

    private File rootDirectory;

    public FileResourceRequestHandler(ServerContext serverContext) {
        String property = System.getProperty("FileResourceRequestHandler.rootDirectory");
        if (property == null) {
            throw new IllegalStateException("Missing \"FileResourceRequestHandler.rootDirectory\" system property pointing to root server resource directory.");
        }
        File file = new File(property);
        if (!file.isDirectory()) {
            throw new IllegalStateException("The \"FileResourceRequestHandler.rootDirectory\" property does not point to directory!");
        }
        rootDirectory = file;

        // Init cache eviction in CacheManager
        CacheManager cacheManager = (CacheManager) serverContext.getAttribute(CacheManager.class.getName());
        if (cacheManager == null) {
            cacheManager = new CacheManager(1);
            serverContext.setAttribute(CacheManager.class.getName(), cacheManager);
        }
        cacheManager.register(authenticationCache, 30);
        cacheManager.register(dataCache, 60);
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, RequestHandlerChain chain) throws Exception {
        File file = extractFileFromPath(request);

        if (file.isDirectory()) {
            listDirectory(request, response, file);
        } else if (file.isFile()) {
            loadFile(request, response, file);
        } else {
            response.setStatus(Status.NOT_FOUND);
        }

        chain.handle(request, response);
    }

    private File extractFileFromPath(HttpRequest request) {
        String uri = request.getUri();
        if (uri.contains("..")) {
            throw new InvalidRequestException(Status.BAD_REQUEST, "Not allowed \"..\" in path!");
        }

        File file = new File(rootDirectory, uri);
        if (!file.exists()) {
            throw new InvalidRequestException(Status.NOT_FOUND, String.format("Specified URI path (%s) not found", uri));
        }

        return file;
    }

    private void listDirectory(HttpRequest request, HttpResponse response, File dir) throws Exception {
        if (isAuthorized(request, response, dir)) {
            StringBuilder sb = new StringBuilder("<html><head><title>");
            sb.append(request.getUri()).append("</title></head><body>")
                .append("<h1>Directory listing:").append(request.getUri()).append("</h1>")
                .append("<ul>");

            for (String file : dir.list()) {
                sb.append("<li><a href=\"")
                    .append(request.getUri());
                if (!request.getUri().endsWith("/")) {
                    sb.append("/");
                }
                sb.append(file).append("\">").append(file).append("</a></li>");
            }

            sb.append("</ul></body></html");

            byte[] bytes = sb.toString().getBytes("utf-8");

            response.setStatus(Status.OK);
            response.setMimeType("text/html");
            response.setContentLength(bytes.length);
            response.write(bytes);
        }
    }

    private void loadFile(HttpRequest request, HttpResponse response, File file) throws Exception {
        if (isAuthorized(request, response, file.getParentFile())) {

            Element element = dataCache.get(file.getAbsolutePath());
            if (element != null) {
                sendFile(response, (FileResource) element.getValue());
                return;
            }

            String fileName = file.getName();
            String contentType = MIME_MAP.getContentType(file);

            FileChannel fileChannel = new RandomAccessFile(file, "r").getChannel();
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());

            long size = fileChannel.size();
            byte[] data = new byte[(int) fileChannel.size()];
            buffer.get(data);

            FileResource fileResource = new FileResource(file.getAbsolutePath(), fileName, size, contentType, data);
            element = new Element(file.getAbsolutePath(), fileResource, size);
            dataCache.putIfAbsent(element);

            sendFile(response, fileResource);
        }
    }

    private void sendFile(HttpResponse response, FileResource fileResource) throws IOException {
        response.setStatus(Status.OK);
        response.setContentLength(fileResource.getContentSize());
        response.setMimeType(fileResource.getContentType());
        response.write(fileResource.getData());
    }

    private boolean isAuthorized(HttpRequest request, HttpResponse response, File dir) throws UnsupportedEncodingException {
        Credentials credentials = loadHtAccess(dir);
        if (credentials == null) {
            return true;
        }

        String authorization = request.getHeader("authorization");
        if (authorization != null && authorization.trim().length() > 6) {
            // trim "Basic dXNlcjpwYXNzd29yZA==" part
            authorization = authorization.substring(6);

            byte[] decodedAuthorization = DECODER.decode(authorization);
            authorization = new String(decodedAuthorization, "utf-8").trim();
            String[] split = authorization.split(":");
            if (split.length == 2) {
                // Check username & password
                String clientUsername = split[0];
                String clientPassword = hashString(split[1]);
                if (credentials.username.equals(clientUsername) && credentials.password.equals(clientPassword)) {
                    return true;
                }
            }
        }

        // Unauthorized
        response.setStatus(Status.UNAUTHORIZED);
        response.addHeader("WWW-Authenticate", "Basic");
        return false;
    }

    private String hashString(String input) throws UnsupportedEncodingException {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(input.getBytes("utf-8"));

            byte byteData[] = md5.digest();

            //convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 hashing algorithm missing ?!?", e);
        }
    }

    private Credentials loadHtAccess(File dir) {
        Element element = authenticationCache.get(dir.getAbsolutePath());
        if (element != null) {
            LOG.debug("Returning cached Credentials for dir ({})", dir.getAbsolutePath());
            return (Credentials) element.getValue();
        }

        File accessFile = new File(dir, HTACCESS_FILE_NAME);
        if (accessFile.exists() && accessFile.isFile()) {
            try {
                List<String> strings = Files.readAllLines(accessFile.toPath());
                if (!strings.isEmpty()) {
                    String credentialsString = strings.get(0);
                    String[] split = credentialsString.split(":");
                    if (split.length == 2) {
                        Credentials cred = new Credentials(split[0], split[1]);
                        Element credElement = new Element(dir.getAbsolutePath(), cred);
                        authenticationCache.putIfAbsent(credElement);
                        LOG.debug("Cached Credentials ({}:{}) for dir ({})", split[0], split[1], dir.getAbsolutePath());
                        return cred;
                    } else {
                        LOG.warn(".htaccess file ({}) contains illegal Credentials format username:hash(password)", accessFile.getAbsolutePath());
                    }
                }
            } catch (IOException ex) {
                LOG.error("Exception occurred during loading of .htaccess file on path ({})", accessFile.getAbsolutePath());
            }
        } else {
            // store null element in cache
            Element nullAuth = new Element(dir.getAbsolutePath(), null);
            authenticationCache.putIfAbsent(nullAuth);
        }

        return null;
    }

    /**
     * Stored Auth Credentials required for folder access.
     */
    private static class Credentials {
        private final String username;
        private final String password;

        private Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

}
