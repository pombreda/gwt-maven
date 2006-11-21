<%@ page import="java.util.*"%>
<%@ page import="java.sql.*"%>
<%@ page import="javax.naming.*" %>
<%@ page import="javax.sql.*" %>

<html>
<head>
<title>Test Derby</title>
<style type="text/css">
 body {
   background-color:#DDDDDD;
   color:#0066FF;
   font-family:Arial,sans-serif;
}
</style>
</head>
<body>

<h3>Test Embedded Derby Initialized by DerbyInitializationServlet</h3>

<%
            Connection conn = null;
            Statement s = null;
            ResultSet rs = null;
           
            // MANUAL TEST            
            try
            {
                out.println("Manual test . . .<br />");
                String framework = "embedded";
                String driver = "org.apache.derby.jdbc.EmbeddedDriver";
                String protocol = "jdbc:derby:";

                Class.forName(driver).newInstance();
                out.println("1. Loaded the driver.<br />");

                Properties props = new Properties();
                props.put("user", "user");
                props.put("password", "password");
                conn = DriverManager.getConnection(protocol + "demo;create=false", props);
                out.println("2. Got a connection.<br />");

                s = conn.createStatement();
                rs = s.executeQuery("SELECT * FROM names ORDER BY id");

                out.println("3. Check that 'Bob' and 'Larry' are in the DB.<br />");
                while (rs.next())
                {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    out.println("&nbsp;&nbsp;&nbsp; id - " + id + " | name - " + name + "<br />");
                }
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
            out.println("4.  Done. If you get here it worked. ");
%>

<br />
<br />
<%
            try
            {
                //JNDI TEST
                out.println("JNDI test . . .<br />");
                Context initCtx = new InitialContext();
                Context envCtx = (Context) initCtx.lookup("java:comp/env");
                DataSource ds = (DataSource) envCtx.lookup("jdbc/derby-demo");
                out.println("1. Got the DataSource.<br />");
                
                conn = ds.getConnection();
                
                s = conn.createStatement();
                rs = s.executeQuery("SELECT * FROM names ORDER BY id");

                out.println("2. Check that 'Bob' and 'Larry' are in the DB.<br />");
                while (rs.next())
                {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    out.println("&nbsp;&nbsp;&nbsp; id - " + id + " | name - " + name + "<br />");
                }                
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
            out.println("3.  Done. If you get here it worked. ");
%>



</body>
</html>
