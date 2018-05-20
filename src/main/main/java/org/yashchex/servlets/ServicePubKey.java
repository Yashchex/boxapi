package org.yashchex.servlets;

import org.yashchex.YashchikCore;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

public class ServicePubKey extends HttpServlet {
    private static final long serialVersionUID = -4751096228274971485L;
    @Override
    protected void doGet(HttpServletRequest reqest, HttpServletResponse response)
            throws ServletException, IOException {
        Credentials credentials = null;
        try {
            credentials = WalletUtils.loadCredentials(YashchikCore.getYASCCHIK().getPassword(), new File(YashchikCore.getYASCCHIK().getCredentialPath()));
        } catch (Exception e) {
            response.getWriter().println(e.toString());
        }
//        return credentials.getAddress();
        response.getWriter().println(credentials.getAddress());
    }
    @Override
    public void init() throws ServletException {
        System.out.println("Servlet " + this.getServletName() + " has started");
    }
    @Override
    public void destroy() {
        System.out.println("Servlet " + this.getServletName() + " has stopped");
    }
}