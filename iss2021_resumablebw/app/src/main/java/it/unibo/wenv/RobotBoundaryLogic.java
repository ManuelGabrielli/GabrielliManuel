/*
===============================================================
RobotBoundaryLogic.java
implements the business logic  

===============================================================
*/
package it.unibo.wenv;
import it.unibo.interaction.MsgRobotUtil;
import it.unibo.supports.IssCommSupport;
import mapRoomKotlin.mapUtil;

import javax.swing.*;

public class RobotBoundaryLogic {
    private IssCommSupport rs ;

private int stepNum              = 1;
private boolean boundaryWalkDone = false ;
private boolean usearil          = false;
private int moveInterval         = 1000;
private RobotMovesInfo robotInfo;
private boolean stop = false;
    //public enum robotLang {cril, aril}    //todo

    public RobotBoundaryLogic(IssCommSupport support, boolean usearil, boolean doMap){
        rs           = support;
        this.usearil = usearil;
        robotInfo    = new RobotMovesInfo(doMap);
        robotInfo.showRobotMovesRepresentation();
    }

    public void doBoundaryGoon(){
        rs.request( usearil ? MsgRobotUtil.wMsg : MsgRobotUtil.forwardMsg  );
        delay(moveInterval ); //to reduce the robot move rate
    }

    public synchronized String doBoundaryInit(){
        System.out.println("RobotBoundaryLogic | doBoundary rs=" + rs + " usearil=" + usearil);
        rs.request( usearil ? MsgRobotUtil.wMsg : MsgRobotUtil.forwardMsg  );
        //The reply to the request is sent by WEnv after the wtime defined in issRobotConfig.txt  
        //delay(moveInterval ); //to reduce the robot move rate
        System.out.println( mapUtil.getMapRep() );
        /*while( ! boundaryWalkDone ) {
            try {
                wait();
                //System.out.println("RobotBoundaryLogic | RESUMES " );
                rs.close();
             } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
        return robotInfo.getMovesRepresentation();
    }

    public void updateMovesRep (String move ){
        robotInfo.updateRobotMovesRepresentation(move);
    }

    public synchronized void stopMovement(){
        stop = true;
    }

    public synchronized void resumeMovement(){
        stop = false;
        doBoundaryGoon();
    }

 //Business logic in RobotBoundaryLogic
    protected synchronized boolean boundaryStep( String move, boolean obstacle ){
        if(!stop) {
            if (stepNum <= 4) {
                if (move.equals("turnLeft")) {
                    updateMovesRep("l");
                    //showRobotMovesRepresentation();
                    if (stepNum == 4) {
                        boundaryWalkDone = true;
                        //run on main thread
                        /*SwingUtilities.invokeLater(new Runnable(){
                            public void run() {
                                robotInfo.showRobotMovesRepresentation();
                                System.exit(0);
                            }
                        });*/
                        robotInfo.showRobotMovesRepresentation();
                        stepNum = 1;
                        return true;
                    }
                    stepNum++;
                    doBoundaryGoon();
                    return false;
                }
                //the move is moveForward
                if (obstacle) {
                    rs.request(usearil ? MsgRobotUtil.lMsg : MsgRobotUtil.turnLeftMsg);
                    delay(moveInterval); //to reduce the robot move rate
                }
                if (!obstacle) {
                    updateMovesRep("w");
                    doBoundaryGoon();
                }
                robotInfo.showRobotMovesRepresentation();
            } else { //stepNum > 4
                System.out.println("RobotBoundaryLogic | boundary ENDS");
            }
        }
        return false;
    }

    protected void delay( int dt ){
        try { Thread.sleep(dt); } catch (InterruptedException e) { e.printStackTrace(); }
    }

}
