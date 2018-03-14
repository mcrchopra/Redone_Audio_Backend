/*==============================================================================

name:       AudioServlet.java

purpose:    Audio servlet.

==============================================================================*/
                                       // package ----------------------------//
package com.pumaj.servlets;
                                       // imports ----------------------------//
import com.google.appengine.api.datastore.*;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.channels.Channels;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;


// AudioServlet ======================//
public class AudioServlet extends HttpServlet
{

private final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
        .initialRetryDelayMillis(10)
        .retryMaxAttempts(10)
        .totalRetryPeriodMillis(15000)
        .build());

// private final GcsService gcsSevice = GcsServiceFactory.createGcsService();


public void doGet(
   HttpServletRequest  req, 
   HttpServletResponse rsp)
   throws ServletException, IOException
{
    Object response = "No";
    doResponse(req, rsp, response);
}
public void doPost (
            HttpServletRequest req,
            HttpServletResponse rsp)
            throws ServletException, IOException
{
    Object response = "Ok";
    saveFile(req);
    doResponse(req,rsp,response);
}

public void saveFile(HttpServletRequest req) throws IOException{
    /*InputStream input = req.getInputStream();
    File targetFile = new File("src/com.pumaj.servlets/targetFile.tmp");
    OutputStream outStream = new ByteArrayOutputStream();

    byte[] buffer = new byte[8 * 1024];
    int bytesRead;
    while ((bytesRead = input.read(buffer)) != -1) {
        outStream.write(buffer, 0, bytesRead);
    }
    input.close();
    outStream.close();*/

    GcsFileOptions instance = GcsFileOptions.getDefaultInstance();
    // GcsFilename fileName = getFileName(req);
    GcsFilename fileName = new GcsFilename("audiowavelet.appspot.com","first.wav");
    GcsOutputChannel outputChannel = gcsService.createOrReplace(fileName, instance);
    copy(req.getInputStream(), Channels.newOutputStream(outputChannel));
    int ja = 0;

}

private GcsFilename getFileName(HttpServletRequest req) {
    String[] splits = req.getRequestURI().split("/", 4);
    if (!splits[0].equals("") || !splits[1].equals("gcs")) {
        throw new IllegalArgumentException("The URL is not formed as expected. " +
                "Expecting /gcs/<bucket>/<object>");
    }
    return new GcsFilename(splits[2], splits[3]);
}

private void copy(InputStream input, OutputStream output) throws IOException {
    try {
        byte[] buffer = new byte[2 * 1024 * 1024];
        int bytesRead = input.read(buffer);
        while (bytesRead != -1) {
            output.write(buffer, 0, bytesRead);
            bytesRead = input.read(buffer);
        }
    } finally {
        input.close();
        output.close();
    }
}


public void doOptions(
   HttpServletRequest  req,
   HttpServletResponse rsp)
   throws ServletException, IOException
{
   doResponse(req, rsp, "OK");
}
/*------------------------------------------------------------------------------

@name       doResponse - generate response
                                                                              */
                                                                             /**
            Write the response back to the requestor.

@return     void

@param      req         servlet request
@param      rsp         servlet response
@param      response    response value

                                                                      */
//------------------------------------------------------------------------------
public void doResponse(
   HttpServletRequest  req, 
   HttpServletResponse rsp,
   Object              response)
   throws ServletException, IOException 
{
                                       // enable cors                         //
   rsp.addHeader("Access-Control-Allow-Origin",  "http://localhost:4200");
   rsp.addHeader("Access-Control-Max-Age",       "1800");
   rsp.addHeader("Access-Control-Allow-Credentials", "true");

                                       // support all requested headers       //
   for (Enumeration<String> ctlReqHeaders =
        req.getHeaders("Access-control-request-headers");
        ctlReqHeaders.hasMoreElements();)
   {
      rsp.addHeader("Access-Control-Allow-Headers", ctlReqHeaders.nextElement());
   }
   if (response instanceof Throwable)
   {
      rsp.sendError(
         HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response.toString());
   }
   else
   {
      rsp.getWriter().print(response);
   }
}


/*------------------------------------------------------------------------------

@name       getRequestParams - get request parameters
                                                                              */
                                                                             /**
            Get request parameters.

@return     request parameters.

                                                                              */
//------------------------------------------------------------------------------
public Map<String,Object> getRequestParams(
   HttpServletRequest  req)
{
   Map<String,String[]> paramsRaw = req.getParameterMap();
   Map<String,Object>   params    = new HashMap<String,Object>();

   for (String key : paramsRaw.keySet())
   {
      params.put(key, paramsRaw.get(key)[0]);
   }

   return(params);
}
}//====================================// end class ComServiceSimpleServlet ------------//
