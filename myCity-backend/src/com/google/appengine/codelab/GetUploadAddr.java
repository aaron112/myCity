package com.google.appengine.codelab;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

public class GetUploadAddr extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BlobstoreService blobstoreService = BlobstoreServiceFactory
	         .getBlobstoreService();

	public void doGet(HttpServletRequest req, HttpServletResponse res)
	         throws ServletException, IOException
	{
		String uploadpath = blobstoreService.createUploadUrl("/upload");
		res.setHeader("Content-Type", "text/plain");
		res.getWriter().write(uploadpath);
		res.getWriter().flush();
		res.getWriter().close();
	}
}