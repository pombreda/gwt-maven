package com.totsp.gwtrefimpl.server;

/*

   Derby - Class SimpleApp

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Properties;

public class SimpleApp
{
    /* the default framework is embedded*/
    public String framework = "embedded";
    public String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    public String protocol = "jdbc:derby:";

    public static void main(String[] args)
    {
        new SimpleApp().go();
    }

    void go()
    {

        System.out.println("SimpleApp starting in " + framework + " mode.");

        try
        {
            /*
               The driver is installed by loading its class.
               In an embedded environment, this will start up Derby, since it is not already running.
             */
            Class.forName(driver).newInstance();
            System.out.println("Loaded the appropriate driver.");

            Connection conn = null;
            Properties props = new Properties();
            props.put("user", "user1");
            props.put("password", "user1");

            /*
               The connection specifies create=true to cause
               the database to be created. To remove the database,
               remove the directory derbyDB and its contents.
               The directory derbyDB will be created under
               the directory that the system property
               derby.system.home points to, or the current
               directory if derby.system.home is not set.
             */
            conn = DriverManager.getConnection(protocol +
                    "names;create=true", props);

            System.out.println("Connected to and created database names");

            conn.setAutoCommit(false);

            /*
               Creating a statement lets us issue commands against
               the connection.
             */
            Statement s = conn.createStatement();

            /*
               We create a table, add a few rows, and update one.
             */
            s.execute("create table names(id int, name varchar(255))");
            System.out.println("Created table names");            
            
            s.execute("insert into names values (1,'Bob')");
            s.execute("insert into names values (2,'Larry')");
            
            ResultSet rs = s.executeQuery(
                    "SELECT * FROM names ORDER BY id");

            while (rs.next())
            {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                System.out.println("id - " + id + " | name - " + name);
            }

            System.out.println("Verified the rows");

            ///s.execute("drop table derbyDB");
            ///System.out.println("Dropped table derbyDB");

            rs.close();
            s.close();            
            conn.commit();
            conn.close();
            

            /*
               In embedded mode, an application should shut down Derby.
               If the application fails to shut down Derby explicitly,
               the Derby does not perform a checkpoint when the JVM shuts down, which means
               that the next connection will be slower.
               Explicitly shutting down Derby with the URL is preferred.
               This style of shutdown will always throw an "exception".
             */
            boolean gotSQLExc = false;

            if (framework.equals("embedded"))
            {
                try
                {
                    DriverManager.getConnection("jdbc:derby:;shutdown=true");
                }
                catch (SQLException se)
                {
                    gotSQLExc = true;
                }

                if (!gotSQLExc)
                {
                    System.out.println("Database did not shut down normally");
                }
                else
                {
                    System.out.println("Database shut down normally");
                }
            }
        }
        catch (Throwable e)
        {
           e.printStackTrace();           
        }

        System.out.println("SimpleApp finished");
    }

    

    
}
