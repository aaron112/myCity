package com.google.appengine.codelab;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

@SuppressWarnings("deprecation")
public class Upload extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BlobstoreService blobstoreService = BlobstoreServiceFactory
	         .getBlobstoreService();

	public void doPost(HttpServletRequest req, HttpServletResponse res)
	         throws ServletException, IOException
	{
		Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(req);
		BlobKey blobKey = blobs.get("myFile");

		res.setHeader("Content-Type", "text/plain");
		if (blobKey.getKeyString().equals(""))
			res.getWriter().write("no such blob");
		else
			res.getWriter().write(blobKey.getKeyString());
		res.getWriter().flush();
		res.getWriter().close();
	}
}