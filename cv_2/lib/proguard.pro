-injars       unknownApplication.jar
-outjars      unknownApplication-obf.jar
-libraryjars  C:\dev\Java\jdk1.7.0_72\jre\lib\rt.jar
-printmapping proguard.map

-keep public class ass.Main {
    public static void main(java.lang.String[]);
}