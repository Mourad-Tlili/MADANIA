package org.interview.demo; // Replace with your actual main application package

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Main Spring Boot application class.
 * Extends SpringBootServletInitializer to support deployment as a WAR file
 * in a standalone servlet container like Apache Tomcat.
 */
@SpringBootApplication
public class DemoApplication extends SpringBootServletInitializer { // <<< EXTEND this class

    /**
     * Used when deploying to a standalone servlet container.
     * Configures the application by pointing to the main application sources.
     *
     * @param application The application builder provided by the servlet container.
     * @return The configured application builder.
     */
    @Override // <<< OVERRIDE this method
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        // Point to your main application class (the one annotated with @SpringBootApplication)
        return application.sources(DemoApplication.class);
    }

    /**
     * Main method, used when running as an executable JAR.
     * This method is still useful for running the application with an embedded server
     * during development if you temporarily switch packaging back to JAR,
     * but it's not directly used when deploying the WAR to an external Tomcat.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
