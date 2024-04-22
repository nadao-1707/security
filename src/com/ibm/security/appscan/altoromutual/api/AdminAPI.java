package com.ibm.security.appscan.altoromutual.api;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.wink.json4j.*;

import com.ibm.security.appscan.altoromutual.util.DBUtil;
import com.ibm.security.appscan.altoromutual.util.ServletUtil;

@Path("/admin")
public class AdminAPI extends AltoroAPI{
    
    @POST
    @Path("/changePassword")
    public Response changePassword(String bodyJSON, @Context HttpServletRequest request) throws IOException{
        JSONObject bodyJson= new JSONObject();
        
        //Don't really care if the user is admin or not - I think that's how it works in AltoroJ
                
        //Convert request to JSON
        String username;
        String password1;
        String password2;
        try {
            bodyJson = new JSONObject(bodyJSON);
            
            //Parse the body for the required parameters
            username = bodyJson.getString("username");
            password1 = bodyJson.getString("password1");
            password2 = bodyJson.getString("password2");
        } catch (JSONException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"Error\": \"Request is not in JSON format\"}").build();
        }
        
        
        //Try to change the password 
        if (username == null || username.trim().isEmpty()
                || password1 == null || password1.trim().isEmpty()
                || password2 == null || password2.trim().isEmpty())
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"An error has occurred. Please try again later.\"}").build();
        
        if (!password1.equals(password2)){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Entered passwords did not match.\"}").build();
        }
    
        String error = null;
        
        if (ServletUtil.getAppProperty("enableAdminFunctions").equalsIgnoreCase("true")) {
            error = changeUserPassword(username, password1);
        }       
        
        if (error != null)
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\""+error+"\"}").build();
        

        return Response.status(Response.Status.OK).entity("{\"success\":\"Requested operation has completed successfully.\"}").type(MediaType.APPLICATION_JSON_TYPE).build();
    }
    
    @POST
    @Path("/addUser")
    public Response addUser(String bodyJSON, @Context HttpServletRequest request) throws IOException{
        JSONObject bodyJson= new JSONObject();
        
        //Checking if user is logged in
        
        String firstname;
        String lastname;
        String username;
        String password1;
        String password2;
                
        //Convert request to JSON
        try {
            bodyJson = new JSONObject(bodyJSON);
            //Parse the request for the required parameters
            firstname = bodyJson.getString("firstname");
            lastname = bodyJson.getString("lastname");
            username = bodyJson.getString("username");
            password1 = bodyJson.getString("password1");
            password2 = bodyJson.getString("password2");
        } catch (JSONException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"Error\": \"Request is not in JSON format\"}").build();
        }
        
        if (username == null || username.trim().isEmpty()
            || password1 == null || password1.trim().isEmpty()
            || password2 == null || password2.trim().isEmpty())
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"An error has occurred. Please try again later.\"}").build();
        
        if (!password1.equals(password2)){
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Entered passwords did not match.\"}").build();
        }
        
        String error = null;
        
        if (ServletUtil.getAppProperty("enableAdminFunctions").equalsIgnoreCase("true")) {
            error = addUser(username, password1, firstname, lastname);
        }       
        
        if (error != null)
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\""+error+"\"}").build();
        
        
        return Response.status(Response.Status.OK).entity("{\"success\":\"Requested operation has completed successfully.\"}").type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    /**
     * Changes the password for the given username.
     * @param username The username for which the password will be changed.
     * @param newPassword The new password.
     * @return Returns an error message if an error occurred, null otherwise.
     */
    private String changeUserPassword(String username, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE username = ?";

        try (PreparedStatement statement = DBUtil.getConnection().prepareStatement(sql)) {
            statement.setString(1, newPassword);
            statement.setString(2, username);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                return "User not found.";
            }
        } catch (SQLException e) {
            return e.getMessage();
        }

        return null;
    }

    /**
     * Adds a new user.
     * @param username The username of the new user.
     * @param password The password of the new user.
     * @param firstName The first name of the new user.
     * @param lastName The last name of the new user.
     * @return Returns an error message if an error occurred, null otherwise.
     */
    private String addUser(String username, String password, String firstName, String lastName) {
        String sql = "INSERT INTO users (username, password, firstname, lastname) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = DBUtil.getConnection().prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, firstName);
            statement.setString(4, lastName);
            statement.executeUpdate();
        } catch (SQLException e) {
            return e.getMessage();
        }

        return null;
    }
}
