/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fanorona.Gui;

import fanorona.Logic.Board;
import static fanorona.Logic.Board.COLS;
import static fanorona.Logic.Board.SPACE;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import static fanorona.Logic.Board.cellSize;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import sun.swing.SwingUtilities2;


/**
 *
 * @author galbenozilio
 */
public class GamePanel extends javax.swing.JPanel {
    
    Image imageBoard;
    Board board; 
    
    /**
     * Creates new form GamePanel
     */
    public GamePanel() {
        initComponents();
        imageBoard = Toolkit.getDefaultToolkit().getImage("images/Empty_Board.png");
        this.board = new Board(this);
    }
    
    @Override
    public void paint(Graphics gr){
        gr.drawImage(imageBoard, 0, 0,this.getWidth(), this.getHeight(), this);
        board.paint(gr);
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                formMousePressed(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                formMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        
    }//GEN-LAST:event_formMouseClicked

    private void formMousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_formMousePressed
    {//GEN-HEADEREND:event_formMousePressed
        int row = (evt.getY() - Board.DIF) / (Board.cellSize+Board.SPACE);
        int col = (evt.getX() - Board.DIF) / (Board.cellSize + Board.SPACE);
        board.Click(row,col);
        repaint();
        board.getOneMoreMove();
        if(board.startAi == true)
            board = board.startAi();
        board.checkWin();
    }//GEN-LAST:event_formMousePressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
