/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fanorona.Gui;

import fanorona.Logic.Board;
import static fanorona.Logic.Board.COLS;
import static fanorona.Logic.Board.SPACE;
import static fanorona.Logic.Board.cellsize;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;


/**
 *
 * @author galbenozilio
 */
public class GamePanel extends javax.swing.JPanel {
    
    Image imageBoard;
    Board board; 
    long selected = 0;
    /**
     * Creates new form GamePanel
     */
    public GamePanel() {
        initComponents();
        imageBoard = Toolkit.getDefaultToolkit().getImage("images/Empty_Board.png");
        this.board = new Board(this);
       // this.setSize(800, 500);
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
    private void initComponents() {

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
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
        int row = (evt.getY() - Board.DIF) / (Board.cellsize+Board.SPACE);
        int col = (evt.getX() - Board.DIF) / (Board.cellsize+Board.SPACE);
        long mask = 1;
        mask<<=(row*9 + col);
        if(board.turn.equals("pw"))
        {
            if(selected != 0)
            {
                if(board.isEmpty(mask))
                {
                    long x = selected;
                    x = ~x;
                    board.white.state &= x;
                    board.white.state |= mask;
                    board.paint(this.getGraphics());
                    selected = 0;
                    board.turn = "pb";
                }
            }
            else if((mask & board.white.state) != 0)
            {
                board.selected(this.getGraphics(), row, col);
                selected = mask;
            }
        }
        else
        {
            if(selected != 0)
            {
                if(board.isEmpty(mask))
                {
                    long x = selected;
                    x = ~x;
                    board.black.state &= x;
                    board.black.state |= mask;
                    board.paint(this.getGraphics());
                    selected = 0;
                    board.turn = "pw";
                }
            }
            else if((mask & board.black.state) != 0)
            {
                board.selected(this.getGraphics(), row, col);
                selected = mask;
            }  
        }    
    }//GEN-LAST:event_formMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
