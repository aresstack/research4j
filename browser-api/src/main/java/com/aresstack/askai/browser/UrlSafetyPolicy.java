package com.aresstack.askai.browser;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Locale;

/**
 * URL gate every backend must apply BEFORE fetching: only http/https; {@code file:}, {@code jar:},
 * {@code data:}, {@code javascript:} (and everything else) are rejected; loopback/private/link-local targets
 * are blocked unless explicitly allowed (tests allow them for the local test server). Resolution failures are
 * rejected, not guessed. A host application can hand in its own {@link HostGate} instead of the public-host rule:
 * then the host alone decides which hosts are reachable (an intranet profile, a question to the user), the scheme
 * rule stays.
 */
public final class UrlSafetyPolicy {

    /**
     * The host's decision about the target of a navigation, after the scheme check: return to admit it, throw a
     * {@link BrowserException} with a readable reason to refuse it. It may block while the host asks its user.
     */
    @FunctionalInterface
    public interface HostGate {
        void admit(URI target) throws BrowserException;
    }

    /** The default gate: only hosts that resolve to a public address. */
    public static final HostGate PUBLIC_HOSTS = target -> requirePublicHost(target.getHost());

    /** The development gate: every host, loopback and private networks included. */
    public static final HostGate ALL_HOSTS = target -> { };

    private final HostGate gate;

    public UrlSafetyPolicy(boolean allowPrivateNetworks) {
        this(allowPrivateNetworks ? ALL_HOSTS : PUBLIC_HOSTS);
    }

    private UrlSafetyPolicy(HostGate gate) {
        this.gate = gate;
    }

    public static UrlSafetyPolicy strict() {
        return new UrlSafetyPolicy(PUBLIC_HOSTS);
    }

    /** Test/dev policy: private/loopback targets explicitly permitted (schemes stay restricted). */
    public static UrlSafetyPolicy allowingPrivateNetworks() {
        return new UrlSafetyPolicy(ALL_HOSTS);
    }

    /** The host decides about every target ({@code null} = {@link #strict()}); schemes stay restricted. */
    public static UrlSafetyPolicy withHostGate(HostGate gate) {
        return new UrlSafetyPolicy(gate == null ? PUBLIC_HOSTS : gate);
    }

    /** @return the parsed URI when safe. @throws BrowserException with a readable reason otherwise. */
    public URI check(String url) throws BrowserException {
        if (url == null || url.trim().isEmpty()) {
            throw new BrowserException("Empty URL.");
        }
        URI uri;
        try {
            uri = new URI(url.trim());
        } catch (URISyntaxException ex) {
            throw new BrowserException("Invalid URL: " + url);
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            throw new BrowserException("Blocked URL scheme: " + (scheme.isEmpty() ? "(none)" : scheme));
        }
        String host = uri.getHost();
        if (host == null || host.isEmpty()) {
            throw new BrowserException("URL has no host: " + url);
        }
        gate.admit(uri);
        return uri;
    }

    private static void requirePublicHost(String host) throws BrowserException {
        InetAddress address;
        try {
            address = InetAddress.getByName(host);
        } catch (UnknownHostException ex) {
            throw new BrowserException("Cannot resolve host: " + host);
        }
        if (address.isLoopbackAddress() || address.isAnyLocalAddress()
                || address.isSiteLocalAddress() || address.isLinkLocalAddress()) {
            throw new BrowserException("Blocked private/loopback target: " + host);
        }
    }
}
