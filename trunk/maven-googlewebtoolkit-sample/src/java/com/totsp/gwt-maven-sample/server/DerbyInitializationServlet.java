package com.totsp.gwtrefimpl.server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

public class DerbyInitializationServlet extends HttpServlet
{

    // we are using embedded because we will hit this only from the same JVM for this gwtrefimpl sample
    // (in the real world you would not do this)
    public String framework = "embedded";
    public String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    public String protocol = "jdbc:derby:";

    public DerbyInitializationServlet()
    {        
        super();
        System.out.println("derby init servlet");
    }

    public String getServletInfo()
    {
        return "This servlet will initialize Derby.";
    }

    public void init() throws ServletException
    { //NOPMD
        super.init();

        // DO STUFF HERE

        Connection conn = null;
        Statement s = null;
        ResultSet rs = null;
        
        try
        {
            /*
            Class.forName(driver).newInstance();
            System.out.println("Loaded the driver.");
            
            Properties props = new Properties();
            props.put("user", "user");
            props.put("password", "password");            
            
            // create a database named "demo"
            conn = DriverManager.getConnection(protocol +
                    "demo;create=true", props);
            */
            
            
            // JNDI TEST
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            DataSource ds = (DataSource) envCtx.lookup("jdbc/derby-demo");
            conn = ds.getConnection();


            // create a table named "names"
            s = conn.createStatement();
            s.execute("create table names(id int, name varchar(255))");
            System.out.println("Created table names");            
            s.execute("insert into names values (1,'Bob')");
            s.execute("insert into names values (2,'Larry')");
            
            rs = s.executeQuery(
                    "SELECT * FROM names ORDER BY id");

            System.out.println("Check that Bob and Larry are in the DB?");
            while (rs.next())
            {                
                int id = rs.getInt("id");
                String name = rs.getString("name");
                System.out.println("id - " + id + " | name - " + name);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                rs.close();
                s.close();            
                conn.commit();
                conn.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) // NOPMD
            throws ServletException, IOException
    {
        this.processRequest(request, response);
    }

    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) // NOPMD
            throws ServletException, IOException
    {
        this.processRequest(request, response);
    }

    protected void processRequest(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException
    {
        throw new ServletException("Thats a no no!");
    }

}
