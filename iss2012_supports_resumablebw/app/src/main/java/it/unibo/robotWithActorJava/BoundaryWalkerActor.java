package it.unibo.robotWithActorJava;


import it.unibo.interaction.IssCommActorSupport;
import it.unibo.supports2021.ActorBasicJava;
import org.json.JSONObject;

public class BoundaryWalkerActor extends ActorBasicJava {
    final String forwardMsg = "{\"robotmove\":\"moveForward\", \"time\": 350}";
    final String backwardMsg = "{\"robotmove\":\"moveBackward\", \"time\": 350}";
    final String turnLeftMsg = "{\"robotmove\":\"turnLeft\", \"time\": 300}";
    final String turnRightMsg = "{\"robotmove\":\"turnRight\", \"time\": 300}";
    final String haltMsg = "{\"robotmove\":\"alarm\", \"time\": 100}";

    //sono gli stati inseriti nel modello
    private enum State {start, walking, obstacle, end, paused };
    private IssCommActorSupport support;
    private State curState       =  State.start ;
    private State oldState;
    private int stepNum          = 1;
    private RobotMovesInfo moves = new RobotMovesInfo(true);
    private boolean robotHalted = true;
    private JSONObject oldMove;

    public BoundaryWalkerActor(String name, IssCommActorSupport support) {
        super(name);
        this.support = support;
    }
/*
    public void startJob(){
        curState       =  State.start;
        //fsm("","");
    }
*/
    public void reset(){
        stepNum        = 1;
        curState       =  State.start;
        robotHalted = true;
        System.out.println("RobotBoundaryLogic | FINAL MAP:"  );
        moves.showRobotMovesRepresentation();
        moves.getMovesRepresentationAndClean();
    }


    //questa funzione rappresenta l'automa a stati finiti
    protected void fsm(String move, String endmove){
        System.out.println( myname + " | fsm state=" + curState + " stepNum=" + stepNum + " move=" + move + " endmove=" + endmove);

        //gestisco lo stop e resume
        if(move.equals("STOP")){
            robotHalted = true;   //halt the robot, not the move
            oldState = curState;
            curState = State.paused;
        }
        if(move.equals("RESUME")){
            robotHalted = false;
            if(curState.equals(State.start)){
                move ="";
            } else{
                curState = oldState;
                fsm(oldMove.getString("move"), oldMove.getString("endmove"));
            }
        }

        switch( curState ) {
            case start: {
                //moves.getMovesRepresentationAndClean();
                moves.showRobotMovesRepresentation(); //mappa delle mosse
                doStep(); //se sono in start faccio direttamente il passo
                curState = State.walking;
                break;
            }
            case walking: {
                if (move.equals("moveForward") && endmove.equals("true")) {
                    //curState = State.walk;
                    moves.updateMovesRep("w");
                    doStep();
                } else if (move.equals("moveForward") && endmove.equals("false")) {
                    curState = State.obstacle;
                    turnLeft();
                } else {System.out.println("IGNORE answer of turnLeft");
                }
                    //if (move.equals("turnLeft") && endmove.equals("true")) doStep();
                break;
            }//walk

            case obstacle :
                //in questo sato ci arriviamo dopo una turn left quindi prima controlliamo
                if( move.equals("turnLeft") && endmove.equals("true")) {
                    if( stepNum < 4) {
                        stepNum++;
                        moves.updateMovesRep("l");
                        moves.showRobotMovesRepresentation();
                        curState = State.walking;
                        doStep();
                    }else{  //at home again
                        curState = State.end;
                        turnLeft(); //to force state transition
                    }
                } break;

            case end : {
                if( move.equals("turnLeft") ) {
                    System.out.println("BOUNDARY WALK END");
                    moves.showRobotMovesRepresentation();
                    turnRight();    //to compensate last turnLeft
                }else{
                    reset();
                }
                break;
            }

            case paused:{
                System.out.println("Journey paused");
                break;
            }

            default: System.out.println("error");
        }
    }


    @Override
    protected void handleInput(String msg ) {     //called when a msg is in the queue
        //System.out.println( name + " | input=" + msgJsonStr);
        if( msg.equals("startApp"))  fsm("","");//inizzializza la macchina
        else msgDriven( new JSONObject(msg) );
    }

    protected void msgDriven( JSONObject infoJson){
        if(!infoJson.has("robotcmd")){
            oldMove = infoJson;
        }
        if( infoJson.has("endmove") )        fsm(infoJson.getString("move"), infoJson.getString("endmove"));
        else if( infoJson.has("sonarName") ) handleSonar(infoJson);
        else if( infoJson.has("collision") ) handleCollision(infoJson);
        else if( infoJson.has("robotcmd") )  handleRobotCmd(infoJson);
    }

    protected void handleSonar( JSONObject sonarinfo ){
        String sonarname = (String)  sonarinfo.get("sonarName");
        int distance     = (Integer) sonarinfo.get("distance");
        //System.out.println("RobotApplication | handleSonar:" + sonarname + " distance=" + distance);
    }
    protected void handleCollision( JSONObject collisioninfo ){
        //we should handle a collision  when there are moving obstacles
        //in this case we could have a collision even if the robot does not move
        //String move   = (String) collisioninfo.get("move");
        //System.out.println("RobotApplication | handleCollision move=" + move  );
    }
  
    protected void handleRobotCmd( JSONObject robotCmd ){
        String cmd = (String)  robotCmd.get("robotcmd");
        if( cmd.equals( "STOP" ) ) {
            if (!robotHalted) {
                fsm(cmd, "");
                System.out.println("RobotApplInputController | handleRobotCmd: robotHalted");
            }
        }
        else if( cmd.equals( "RESUME" ) ){
            if( robotHalted ) {
               fsm(cmd, "");
            }else
                System.out.println("RobotApplInputController | handleRobotCmd robot already running"    );
        }
        System.out.println("===================================================="    );
        System.out.println("RobotApplication | handleRobotCmd cmd=" + cmd  );
        System.out.println("===================================================="    );
    }

    //------------------------------------------------
    protected void doStep(){
        support.forward( forwardMsg);
        delay(1000); //to avoid too-rapid movement
    }
    protected void turnLeft(){
        support.forward( turnLeftMsg );
        delay(1000); //to avoid too-rapid movement
    }
    protected void turnRight(){
        support.forward( turnRightMsg );
        delay(500); //to avoid too-rapid movement
    }
}
