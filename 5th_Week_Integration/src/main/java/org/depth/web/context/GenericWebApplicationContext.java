package org.depth.web.context;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.depth.beans.factory.ListableBeanFactory;
import org.depth.beans.factory.context.GenericApplicationContext;
import org.depth.web.servlet.Servlet;

@NoArgsConstructor
public class GenericWebApplicationContext extends GenericApplicationContext {
    @Getter @Setter
    private Servlet servlet;

    public GenericWebApplicationContext(Servlet servlet) {
        this.servlet = servlet;
    }

    public GenericWebApplicationContext(ListableBeanFactory beanFactory, Servlet servlet) {
        super(beanFactory);
        this.servlet = servlet;
    }

    @Override
    public String getApplicationName() {
        return (this.servlet != null ? this.servlet.getServletPath() : "");
    }
}
