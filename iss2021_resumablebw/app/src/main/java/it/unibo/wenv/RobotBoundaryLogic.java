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

public class RobotBoundaryLogic {
    private IssCommSupport rs ;

    private int stepNum              = 1;
    private boolean boundaryWalkDone = false ;
    private boolean usearil          = false;
    private int moveInterval         = 1000;
    private RobotMovesInfo robotInfo;
    //public enum robotLang {cril, aril}    //todo
    private boolean journeyHalted = true;

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

    public void reset(){
        stepNum          = 1;
        boundaryWalkDone = false;
        setJourneyHalted(true);
        System.out.println("RobotBoundaryLogic | FINAL MAP:"  );
        robotInfo.showRobotMovesRepresentation();
        robotInfo.getMovesRepresentationAndClean();
        robotInfo.showRobotMovesRepresentation();
    }

    public void setJourneyHalted(boolean halted){
        this.journeyHalted = halted;
    }

    public boolean getJourneyHalted(){
        return this.journeyHalted;
    }

    public synchronized String doBoundaryInit(){
        System.out.println("RobotBoundaryLogic | doBoundary rs=" + rs + " usearil=" + usearil);
        //rs.request( usearil ? MsgRobotUtil.wMsg : MsgRobotUtil.forwardMsg  );
        //The reply to the request is sent by WEnv after the wtime defined in issRobotConfig.txt
        //delay(moveInterval ); //to reduce the robot move rate
        System.out.println( mapUtil.getMapRep() );
        while( ! boundaryWalkDone ) {
            try {
                wait();
                //System.out.println("RobotBoundaryLogic | RESUMES " );
                rs.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return robotInfo.getMovesRepresentationAndClean();
    }

    public void updateMovesRep (String move ){
        robotInfo.updateRobotMovesRepresentation(move);
    }

    //Business logic in RobotBoundaryLogic
    protected synchronized void boundaryStep( String move, boolean obstacle){
        if (stepNum <= 4) {
            if( move.equals("turnLeft") ){
                updateMovesRep("l");
                //robotInfo.showRobotMovesRepresentation();
                if (stepNum == 4) {
                    System.out.println("RobotBoundaryLogic | boundary ENDS"  );
                    reset();
                    return;
                }
                stepNum++;
                if( !journeyHalted)  doBoundaryGoon();
                return;
            }
            //the move is moveForward
            if( obstacle && !journeyHalted){
                rs.request( usearil ? MsgRobotUtil.lMsg : MsgRobotUtil.turnLeftMsg   );
                delay(moveInterval ); //to reduce the robot move rate
            }else if( ! obstacle ){
                updateMovesRep("w");
                if( !journeyHalted) doBoundaryGoon();
            }
            robotInfo.showRobotMovesRepresentation();
        }else{ //stepNum > 4
            System.out.println("RobotBoundaryLogic | boundary ENDS"  );
        }
    }

    protected void delay( int dt ){
        try { Thread.sleep(dt); } catch (InterruptedException e) { e.printStackTrace(); }
    }

}
