package ir.ac.sbu.link;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LinkUtility {

    private static final String MD5_ALGORITHM = "MD5";

    private LinkUtility() {
    }

    /**
     * Extracts domain from a URL
     *
     * @param link link URL (URL must be in absolute format)
     * @return domain of url without it's subdomains
     * @throws MalformedURLException if link is not a illegal url
     */
    public static String getMainDomain(String link) throws MalformedURLException {
        try {
            String domain = getDomain(link);
            int lastDot = domain.lastIndexOf('.');
            int beforeLastDot = domain.substring(0, lastDot).lastIndexOf('.');
            return beforeLastDot == -1 ? domain : domain.substring(beforeLastDot + 1);
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            throw new MalformedURLException("Invalid URL: " + link);
        }
    }

    public static String getDomain(String link) {
        int indexOfProtocol = link.indexOf('/') + 1;
        int indexOfEndDomain = link.indexOf('/', indexOfProtocol + 1);
        if (indexOfEndDomain < 0) {
            indexOfEndDomain = link.length();
        }
        String domain = link.substring(indexOfProtocol + 1, indexOfEndDomain);
        int colonIndex = domain.indexOf(':');
        if (colonIndex > -1) {
            domain = domain.substring(0, colonIndex);
        }
        return domain;
    }

    public static boolean isValidUrl(String link) {
        try {
            URI uri = new URL(link).toURI();
            return uri.getHost() != null && uri.getHost().split("\\.").length >= 2;
        } catch (MalformedURLException | URISyntaxException | NullPointerException e) {
            return false;
        }
    }

    public static String normalize(String link) throws MalformedURLException {
        URL url = new URL(link);
        String protocol = url.getProtocol().toLowerCase();
        String host = url.getHost().toLowerCase();
        int port = url.getPort();
        String uri = url.getPath();
        String newLink = protocol + "://" + host;
        if (port != -1) {
            newLink += ":" + port;
        }
        if (uri != null) {
            if (uri.endsWith("/")) {
                newLink += uri.substring(0, uri.length() - 1);
            } else {
                newLink += uri;
            }
        }
        return newLink;
    }

    public static String hashLink(String url) {
        byte[] digest = getHashAlgorithm().digest(url.getBytes());
        BigInteger number = new BigInteger(1, digest);
        StringBuilder hashText = new StringBuilder(number.toString(16));
        while (hashText.length() < 32) {
            hashText.insert(0, "0");
        }
        return hashText.toString();
    }

    public static String hashLinkCompressed(String url) {
        byte[] digest = getHashAlgorithm().digest(url.getBytes());
        return new String(digest, StandardCharsets.UTF_8);
    }

    private static MessageDigest getHashAlgorithm() {
        try {
            return MessageDigest.getInstance(MD5_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError("Hash algorithm does not exists: " + MD5_ALGORITHM);
        }
    }
}

