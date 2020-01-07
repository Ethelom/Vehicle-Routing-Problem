package dmst.mebede.group12.vrp;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

/**
 *
 * @author mzaxa
 */
public class SolutionDrawer {

    static void drawRoutes(ArrayList <Node> allNodes, String fileName, ArrayList<Route> routes)
    {

        int VRP_Y = 800;
        int VRP_INFO = 200;
        int X_START = 200;
        int X_GAP = 600;
        int Y_START = 200;
        int Y_GAP = 800;
        int margin = 30, VEH_INDEX;
        int marginNode = 1;
        int drawMulti = 12;

        int XXX =  VRP_INFO + X_GAP;
        int YYY =  VRP_Y;


        BufferedImage output = new BufferedImage(XXX, YYY, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = output.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, XXX, YYY);
        g.setColor(Color.BLACK);


        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        for (int i = 0; i < allNodes.size(); i++)
        {
            Node n = allNodes.get(i);
            if (n.getX() > maxX) {
                maxX = n.getX();
            }
            if (n.getX() < minX) {
                minX = n.getX();
            }
            if (n.getY() > maxY) {
                maxY = n.getY();
            }
            if (n.getY() < minY) {
                minY = n.getY();
            }
        }

        int mX = XXX - 2 * margin;
        int mY = VRP_Y - 2 * margin;

        int A, B;
        if ((maxX - minX) > (maxY - minY))
        {
            A = mX;
            B = (int)((A) * (maxY - minY) / (maxX - minX));
            if (B > mY)
            {
                B = mY;
                A = (int)((B) * (maxX - minX) / (maxY - minY));
            }
        }
        else
        {
            B = mY;
            A = (int)((B) * (maxX - minX) / (maxY - minY));
            if (A > mX)
            {
                A = mX;
                B = (int)((A) * (maxY - minY) / (maxX - minX));
            }
        }

        for (int r = 0; r < routes.size(); r++) {
            Route rt = routes.get(r);
            for (int i = 1; i < rt.getRouteNodes().size(); i++) {
                Node n;
                n = rt.getRouteNodes().get(i - 1);
                int ii1 = (int) ((A) * ((n.getX() - minX) / (maxX - minX) - 0.5) + (double) mX / 2) + margin;
                int jj1 = (int) ((B) * (0.5 - (n.getY() - minY) / (maxY - minY)) + (double) mY / 2) + margin;
                n = rt.getRouteNodes().get(i);
                int ii2 = (int) ((A) * ((n.getX() - minX) / (maxX - minX) - 0.5) + (double) mX / 2) + margin;
                int jj2 = (int) ((B) * (0.5 - (n.getY() - minY) / (maxY - minY)) + (double) mY / 2) + margin;

                g.setColor(Color.BLACK);
                g.drawLine(ii1, jj1, ii2, jj2);
            }
        }

        for (int i = 0; i < allNodes.size(); i++)
        {
            Node n = allNodes.get(i);

            int ii = (int)((A) * ((n.getX() - minX) / (maxX - minX) - 0.5) + (double)mX / 2) + margin;
            int jj = (int)((B) * (0.5 - (n.getY() - minY) / (maxY - minY)) + (double)mY / 2) + margin;
            if (i != 0)
            {
                g.fillOval(ii - 2 * marginNode, jj - 2 * marginNode, 4 * marginNode, 4 * marginNode);
                String id = Integer.toString(n.getNodeID());
                g.drawString(id, ii + 8 * marginNode, jj+ 8 * marginNode);
            }
            else
            {
                g.fillRect(ii - 4 * marginNode, jj - 4 * marginNode, 8 * marginNode, 8 * marginNode);
                String id = Integer.toString(n.getNodeID());
                g.drawString(id, ii + 8 * marginNode, jj + 8 * marginNode);
            }
        }

        String cst = "Cost: " + routes.get(2).getTotalRouteTimeInHrs();
        g.drawString(cst, 10, 10);

        fileName = fileName + ".png";
        File f = new File(fileName);
        try
        {
            ImageIO.write(output, "PNG", f);
        } catch (IOException ex) {
            Logger.getLogger(VehicleRoutingProblem.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
