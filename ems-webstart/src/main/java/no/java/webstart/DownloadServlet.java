package no.java.webstart;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.InputStream;
import java.net.URL;
import java.net.URI;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class DownloadServlet extends HttpServlet {
    private static final String HEADER_LASTMOD = "Last-Modified";
    private static final String HEADER_JNLP_VERSION = "x-java-jnlp-version-id";
    private static final String JAR_MIME_TYPE = "application/x-java-archive";
    public static final String CONTENT_ENCODING = "content-encoding";
    public static final String PACK200_GZIP_ENCODING = "pack200-gzip";

    private final long lastModified = System.currentTimeMillis();
    private String template;
    private String codebase;
    private String submitItURI;

    @Override
    public void init() throws ServletException {
        template = getServletConfig().getInitParameter("template");
        codebase = getServletConfig().getInitParameter("codebase");
        submitItURI = System.getProperty("session-browse-uri");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /*long modifiedSince = req.getDateHeader("If-Modified-Since");
        if (modifiedSince > 0) {
            if (lastModified == modifiedSince) {
                resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            }
        }
        else {*/
        //TODO: If-Modified-Since support.
        String requestPath = req.getRequestURI();
        if (requestPath.endsWith(".pack.gz")) {
            String id = req.getParameter("version-id");
            if (id != null) {
                String[] versions = id.split(",");
                id = versions[0];
            }
            else {
                id = "";
            }
            String filename = requestPath.substring(requestPath.indexOf(codebase));
            String extension = ".jar.pack.gz";
            String basename = filename.substring(0, filename.indexOf(extension));
            String realFilename = basename + "-" + id + extension;
            URL resource = getServletContext().getResource(realFilename);
            InputStream stream = resource.openStream();
            resp.setContentType(JAR_MIME_TYPE);
            resp.setHeader(CONTENT_ENCODING, PACK200_GZIP_ENCODING);
            if (StringUtils.isNotBlank(id)) {
                resp.setHeader(HEADER_JNLP_VERSION, id);
            }
            try {
                IOUtils.copy(stream, resp.getOutputStream());
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }
        else {
            try {
                handlejnlpRequest(req, resp);
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
    }

    private void handlejnlpRequest(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setCharacterEncoding("UTF-8");
        URL template = getServletContext().getResource(this.template);
        if (template != null) {
            resp.setDateHeader(HEADER_LASTMOD, lastModified);
            resp.setContentType("application/x-java-jnlp-file");
            resp.setStatus(HttpServletResponse.SC_OK);
            specializeJnlpTemplate(req, resp, IOUtils.toString(template.openStream()));
        }
        else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, template + " was not found");
        }
    }

    private void specializeJnlpTemplate(HttpServletRequest request, HttpServletResponse resp, String jnlpTemplate) throws Exception {
        StringWriter writer = new StringWriter();
        URI baseURI = getBaseURI(request);
        VelocityEngine engine = new VelocityEngine();        
        engine.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.ServletLogChute");
        engine.setProperty("runtime.log.logsystem.servlet.level", "info");
        engine.setApplicationAttribute("javax.servlet.ServletContext", getServletContext());
        engine.init();
        VelocityContext context = new VelocityContext();
        context.put("name", "launch.jnlp");
        context.put("codebase", baseURI + request.getContextPath() + codebase);
        context.put("emsURI", baseURI + request.getContextPath() + "/ems");
        context.put("sessionURI", submitItURI);
        if (engine.evaluate(context, writer, "jnlp", jnlpTemplate)) {
            String jnlp = writer.toString();
            jnlp = jnlp.replace(".jar", ".jar.pack.gz");
            resp.getWriter().write(jnlp);
        }
        else {
            throw new IllegalStateException("Unable to render jnlp");
        }
    }

    private URI getBaseURI(HttpServletRequest req) {
        StringBuilder sb = new StringBuilder();
        String scheme = req.getScheme();
        int port = req.getServerPort();
        sb.append(scheme);        // http, https
        sb.append("://");
        sb.append(req.getServerName());
        if (("http".equals(scheme) && port != 80)
                || ("https".equals(scheme) && port != 443)) {
            sb.append(':');
            sb.append(req.getServerPort());
        }
        return URI.create(sb.toString());
    }

}
