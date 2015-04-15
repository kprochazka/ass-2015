package ass.pools.prochka6;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        final QueueSimpleObjectPool<User> pool = new QueueSimpleObjectPool<>();

        new Thread() {
            @Override
            public void run() {
                while (true) {
                    System.gc();
                    try {
                        User user = pool.borrowObject();
                        System.out.println("Borrowed: " + user);
                        user = null;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    System.gc();
                    try {
                        User user = pool.borrowObject();
                        System.out.println("Borrowed: " + user);
                        user = null;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    System.gc();
                    try {
                        User user = pool.borrowObject();
                        System.out.println("Borrowed: " + user);
                        user = null;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    System.gc();
                    try {
                        User user = pool.borrowObject();
                        System.out.println("Borrowed: " + user);
                        user = null;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    System.gc();
                    try {
                        User user = pool.borrowObject();
                        System.out.println("Borrowed: " + user);
                        user = null;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }.start();

        Thread.sleep(1000);

        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 1_000_000; i++) {
                    System.gc();

                    User
                        user =
                        new User(
                            new String("lorem ipsum dolor sit amen, pul mehod atoc metorc ketor fetor zetor, hakac, pes kocka dum ="), i);
                    System.out.println("Offered: " + user);
                    pool.offer(user);
                    user = null;
                }
            }
        }.start();

    }

}
