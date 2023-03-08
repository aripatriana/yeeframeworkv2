package com.yeeframework.automate.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

import org.springframework.expression.spel.support.ReflectionHelper;

public class ClassResolver {

	/**
     * The package resolver used to retrieve URLs to packages
     */
    /**
     * Gets a list of all classes in the given package and all subpackages
     * recursively.
     * 
     * @param pkg the package
     * @param classLoader the class loader to use
     * @return the list of classes
     * @throws IOException if a subpackage or a class could not be loaded
     */
    public static List<Class<?>> getClassesFromPackage(String pkg,
            ClassLoader classLoader) throws IOException {
        return getClassesFromPackage(pkg, classLoader, true);
    }
    /**
     * Gets a list of all classes in the given package
     * 
     * @param pkg the package
     * @param classLoader the class loader to use
     * @param recursive true if all subpackages shall be traversed too
     * @return the list of classes
     * @throws IOException if a subpackage or a class could not be loaded
     */
    public static List<Class<?>> getClassesFromPackage(String pkg,
            ClassLoader classLoader, boolean recursive) throws IOException {
        List<Class<?>> result = new ArrayList<Class<?>>();
        getClassesFromPackage(pkg, result, classLoader, recursive);
        return result;
    }
    private static void getClassesFromPackage(String pkg, List<Class<?>> l,
            ClassLoader classLoader, boolean recursive) throws IOException {
        File[] files = getFilesFromPackage(classLoader, pkg);
        for (File f : files) {
            String name = f.getName();
            if (f.isDirectory() && recursive) {
                if (!name.startsWith(".")) { //$NON-NLS-1$
                    getClassesFromPackage(
                            pkg + "." + name, l, classLoader, true); //$NON-NLS-1$
                }
            } else if (name.toLowerCase().endsWith(".class")) { //$NON-NLS-1$
                // the following lines make sure we also handle classes
                // in subpackages. These subpackages may be returned by
                // ApplicationContext.getFilesFromPackage() when we are
                // in a jar file
                String classPath = f.toURI().toString().replace('/', '.')
                        .replace('\\', '.');
                String className = classPath.substring(
                        classPath.lastIndexOf(pkg),
                        classPath.lastIndexOf('.'));
                Class<?> c;
                try {
                    c = Class.forName(className, true, classLoader);
                } catch (ClassNotFoundException e) {
                    throw new IOException("Could not load class: " + //$NON-NLS-1$
                            e.getMessage());
                }

                l.add(c);
            }
        }
    }
    /**
     * Returns an array of all files contained by a given package
     * 
     * @param pkg the package (e.g. "de.igd.fhg.CityServer3D")
     * @return an array of files
     * @throws IOException if the package could not be found
     */
    public static synchronized File[] getFilesFromPackage(ClassLoader classLoader, String pkg)
            throws IOException {

        File[] files;
        JarFile jarFile = null;
        try {
            URL u = ClassResolver.class.getResource("/" + pkg.replaceAll("[.]", "/"));
            if (u != null && !u.toString().startsWith("jar:")) { //$NON-NLS-1$
                // we got the package as an URL. Simply create a file
                // from this URL
                File dir;
                try {
                    dir = new File(u.toURI());
                } catch (URISyntaxException e) {
                    // if the URL contains spaces and they have not been
                    // replaced by %20 then we'll have to use the following line
                    dir = new File(u.getFile());
                }
                if (!dir.isDirectory()) {
                    // try another method
                    dir = new File(u.getFile());
                }
                files = null;
                if (dir.isDirectory()) {
                    files = dir.listFiles();
                }
            } else {
                // the package may be in a jar file
                // get the current jar file and search it
                if (u != null && u.toString().startsWith("jar:file:")) { //$NON-NLS-1$
                    // first try using URL and File
                    try {
                        String p = u.toString().substring(4);
                        p = p.substring(0, p.indexOf("!/")); //$NON-NLS-1$
                        File file = new File(URI.create(p));
                        p = file.getAbsolutePath();
                        try {
                            jarFile = new JarFile(p);
                        } catch (ZipException e) {
                            throw new IllegalArgumentException(
                                    "No zip file: " + p, e); //$NON-NLS-1$
                        }
                    } catch (Throwable e1) {
                        // second try directly using path
                        String p = u.toString().substring(9);
                        p = p.substring(0, p.indexOf("!/")); //$NON-NLS-1$
                        try {
                            jarFile = new JarFile(p);
                        } catch (ZipException e2) {
                            throw new IllegalArgumentException(
                                    "No zip file: " + p, e2); //$NON-NLS-1$
                        }
                    }
                } else {
                    u = getCurrentJarURL();

                    // open jar file
                    JarURLConnection juc = (JarURLConnection) u
                            .openConnection();
                    jarFile = juc.getJarFile();
                }

                // enumerate entries and add those that match the package path
                Enumeration<JarEntry> entries = jarFile.entries();
                ArrayList<String> file_names = new ArrayList<String>();
                String package_path = pkg.replaceAll("\\.", "/"); //$NON-NLS-1$ //$NON-NLS-2$
                boolean slashed = false;
                if (package_path.charAt(0) == '/') {
                    package_path = package_path.substring(1);
                    slashed = true;
                }
                while (entries.hasMoreElements()) {
                    JarEntry j = entries.nextElement();
                    if (j.getName().matches("^" + package_path + ".+\\..+")) { //$NON-NLS-1$ //$NON-NLS-2$
                        if (slashed) {
                            file_names.add("/" + j.getName()); //$NON-NLS-1$
                        } else {
                            file_names.add(j.getName());
                        }
                    }
                }

                // convert list to array
                files = new File[file_names.size()];
                Iterator<String> i = file_names.iterator();
                int n = 0;
                while (i.hasNext()) {
                    files[n++] = new File(i.next());
                }
            }
        } catch (Throwable e) {
            throw new IOException("Could not find package: " + pkg, e); //$NON-NLS-1$
        } finally {
            if (jarFile != null) {
                jarFile.close();
            }
        }

        if (files != null && files.length == 0)
            return null; // let's not require paranoid callers

        return files;
    }
    /**
     * @return the URL to the JAR file this class is in or null
     * @throws MalformedURLException if the URL to the jar file could not be
     *             created
     */
    public static URL getCurrentJarURL() throws MalformedURLException {
        String name = ReflectionHelper.class.getCanonicalName();
        name = name.replaceAll("\\.", "/"); //$NON-NLS-1$ //$NON-NLS-2$
        name = name + ".class"; //$NON-NLS-1$
        URL url = ReflectionHelper.class.getClassLoader().getResource(name);
        String str = url.toString();
        int to = str.indexOf("!/"); //$NON-NLS-1$
        if (to == -1) {
            url = ClassLoader.getSystemResource(name);
            if (url != null) {
                str = url.toString();
                to = str.indexOf("!/"); //$NON-NLS-1$
            } else {
                return null;
            }
        }
        return new URL(str.substring(0, to + 2));
    }
}
