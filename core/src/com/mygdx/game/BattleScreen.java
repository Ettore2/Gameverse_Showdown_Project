package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;

import java.util.Vector;

import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled;

public class BattleScreen extends GameState {
    private static final boolean CAN_USE_DEBUG_FOR_P1 = false;
    private static boolean USE_DEBUG_FOR_P1 = false;
    private static final int DEBUG_SNSIBILITY_DELAY = 20;
    private static int DEBUG_ACTIVATION_FRAME_TIMER = 0;
    private static int DEBUG_FRAME_TIMER = 0;
    private static final float imgResumeAlphaValMultiplier = 2.5f;
    public final int STATE_BATTLE = 0;
    public final int STATE_WIN_P1 = 1;
    public final int STATE_WIN_P2 = 2;
    public final int STATE_DAW = 3;//(pareggio)
    public final int STATE_PAUSE = 4;

    static final int START_BUTTON_DELAY = 15;//faccio un delay custom perché deve essere condiviso tra i 2 player
    private Stage stageBackGround;
    private Batch stageBackGroundBatch;
    Character personaggio1, personaggio2;
    BattleStage battleStage;
    int currentState;
    int framesPassedByLastStartPress;
    float secondsToMatchEnd;
    private ProgressBar health1;
    private ProgressBar health2;
    private ProgressBar stamina1;
    private ProgressBar stamina2;
    private Image pg1Icn;
    private Image pg2Icn;
    private Image pg1Name;
    private Image pg2Name;



    private Environment environment;
    private PerspectiveCamera camera3D; //(camera2D è gia presente in classe stage)
    private ModelBatch modelBatch;
    private Array<ModelInstance> instances;
    private CameraInputController cameraController;

    //usato per disegnare colliders e rilevare le collisioni (assicurarsi che tutti i collider creati siano presenti qui)
    Vector<Collider2D> existingColliders;


    //immagini
    Image imgPause, imgP1Win, imgP2Win, imgDraw, imgResume;
    float imgResumeAlphaValTimer;
    private Skin healthSkin;
    private Skin staminaSkin;

    //timer timer
    Label lblTimer;
    int lblTimerHeight;





    BattleScreen(final Main game,int idBattleStage, int idC1, int idC2, float matchTime){
        super(game);
        setInputsDelay(0);
        this.secondsToMatchEnd = matchTime;
        USE_DEBUG_FOR_P1 = CAN_USE_DEBUG_FOR_P1;//debug frame per frame

        this.stageBackGround = new Stage();
        this.stageBackGroundBatch = stageBackGround.getBatch();
        this.currentState = STATE_BATTLE;
        this.framesPassedByLastStartPress = START_BUTTON_DELAY;

        stageBackGround.getCamera().viewportWidth = (float)Gdx.graphics.getWidth();
        stageBackGround.getCamera().viewportHeight = (float)Gdx.graphics.getHeight();
        stageBackGround.getCamera().position.set(stageBackGround.getCamera().viewportWidth/2, stageBackGround.getCamera().viewportHeight/2,0);


        existingColliders = new Vector<>();

        // Create an environment so we have some lighting
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1f, 1f));
        environment.add(new DirectionalLight().set(0.4f, 0.4f, 0.4f, -1f, -0.8f, -0.2f));

        modelBatch = new ModelBatch();

        // Create a perspective camera with some sensible defaults
        camera3D = new PerspectiveCamera(50, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        //camera3D.position.set(0, 1.3f, 4);
        camera3D.position.set(0, 1.3f, 5.5f);
        camera3D.lookAt(0, 2, 0);
        camera3D.near = 1f;
        camera3D.far = 300f;
        camera3D.update();

        instances = new Array<>();
        //instances.add(new ModelInstance(new G3dModelLoader(new JsonReader()).loadModel(Gdx.files.internal("BattleFields/Stage1/fighting_polygon_stage.g3dj"))));

        //cameraController = new CameraInputController(camera3D);
        //cameraController.forwardKey= Input.Keys.G;
        //cameraController.backwardKey= Input.Keys.V;
        //cameraController.rotateLeftKey= Input.Keys.C;
        //cameraController.rotateRightKey= Input.Keys.B;
        //Gdx.input.setInputProcessor(cameraController);


        Character.groundHeight = 0.086f;
        battleStage = new BattleStage(camera3D, idBattleStage,idC1,idC2, existingColliders);
        stageBackGround.addActor(battleStage);

        instances.add(battleStage.c1);
        instances.add(battleStage.c2);
        personaggio1 = battleStage.c1;
        personaggio2 = battleStage.c2;

        healthSkin = new Skin(Gdx.files.internal(GameConstants.SKIN_PROGRESSBAR));
        staminaSkin = new Skin(Gdx.files.internal(GameConstants.SKIN_PROGRESSBAR));
        health1 = new ProgressBar(0f, personaggio1.maxLife, 1f, false, healthSkin, "horizontal-lifeC1");
        health1.setSize(GameConstants.screenWidth * 0.3f, GameConstants.screenHeight * 0.009f);
        health1.setPosition(GameConstants.screenWidth * 0.1f, GameConstants.screenHeight * 0.89f);

        health2 = new ProgressBar(0f, personaggio2.maxLife, 1f, false, healthSkin, "horizontal-lifeC2");
        health2.setSize(GameConstants.screenWidth*0.3f, GameConstants.screenHeight*0.009f);
        health2.setPosition(GameConstants.screenWidth * 0.6f, health1.getY());

        stamina1 = new ProgressBar(0f, personaggio1.maxGuardAmount, 1f, false, staminaSkin, "horizontal-StaminaC1");
        stamina1.setSize(GameConstants.screenWidth*0.3f, GameConstants.screenHeight*0.009f);
        stamina1.setPosition(health1.getX(), health1.getY()*0.95f);

        stamina2 = new ProgressBar(0f, personaggio2.maxGuardAmount,1f, false, staminaSkin, "horizontal-StaminaC2");
        stamina2.setSize(GameConstants.screenWidth*0.3f, GameConstants.screenHeight*0.009f);
        stamina2.setPosition(health2.getX(), stamina1.getY());

        pg1Icn = new Image(new Texture("Img/Characters/" + personaggio1.CHARACTERS_NAMES[personaggio1.id] + "/" + personaggio1.CHARACTERS_NAMES[personaggio1.id] + ".png"));
        pg1Icn.setSize(GameConstants.screenWidth*0.088f, GameConstants.screenHeight*0.168f);

        pg2Icn = new Image(new Texture("Img/Characters/" + personaggio2.CHARACTERS_NAMES[personaggio2.id] + "/" + personaggio2.CHARACTERS_NAMES[personaggio2.id] + "_isOverC2.png"));
        pg2Icn.setSize(GameConstants.screenWidth * 0.088f, GameConstants.screenHeight*0.168f);
        pg2Icn.setScale(-1,1);

        pg1Icn.setPosition(GameConstants.screenWidth*0.005f, GameConstants.screenHeight*0.822f);
        pg2Icn.setPosition(GameConstants.screenWidth - pg1Icn.getX() , pg1Icn.getY());

        pg1Name = new Image(new Texture("Img/CharaName/" + personaggio1.CHARACTERS_NAMES[personaggio1.id] + ".png"));
        pg1Name.setScale(0.4f);
        pg1Name.setPosition(GameConstants.screenWidth * 0.098f, GameConstants.screenHeight * 0.915f);

        pg2Name = new Image(new Texture("Img/CharaName/" + personaggio2.CHARACTERS_NAMES[personaggio2.id] + ".png"));
        pg2Name.setScale(0.4f);
        pg2Name.setPosition(GameConstants.screenWidth - GameConstants.screenWidth * 0.098f - (pg2Name.getWidth()*pg2Name.getScaleX()), GameConstants.screenHeight * 0.915f);



        stageBackGround.addActor(health1);
        stageBackGround.addActor(health2);
        stageBackGround.addActor(stamina1);
        stageBackGround.addActor(stamina2);
        stageBackGround.addActor(pg1Icn);
        stageBackGround.addActor(pg2Icn);
        stageBackGround.addActor(pg1Name);
        stageBackGround.addActor(pg2Name);

        //immagini
        imgPause = new Image(new Texture("Img/battleScreen/Pause.png"));
        imgP1Win = new Image(new Texture("Img/battleScreen/P1WINS.png"));
        imgP2Win = new Image(new Texture("Img/battleScreen/P2WINS.png"));
        imgDraw = new Image(new Texture("Img/battleScreen/Draw.png"));
        imgResume = new Image(new Texture("Img/battleScreen/Resume.png"));

        imgPause.setPosition(stageBackGround.getCamera().position.x - (imgPause.getWidth()*imgResume.getScaleX()/2),stageBackGround.getCamera().position.y - GameConstants.screenHeight * 0.252f);
        imgP1Win.setPosition(stageBackGround.getCamera().position.x - (imgP1Win.getWidth()*imgResume.getScaleX()/2),stageBackGround.getCamera().position.y + GameConstants.screenHeight * 0.05f);
        imgP2Win.setPosition(stageBackGround.getCamera().position.x - (imgP2Win.getWidth()*imgResume.getScaleX()/2),stageBackGround.getCamera().position.y + GameConstants.screenHeight * 0.05f);
        imgDraw.setPosition(stageBackGround.getCamera().position.x - (imgDraw.getWidth()*imgResume.getScaleX()/2),stageBackGround.getCamera().position.y + GameConstants.screenHeight * 0.05f);
        imgResume.setScale(0.5f);
        imgResume.setPosition(stageBackGround.getCamera().position.x - (imgResume.getWidth()*imgResume.getScaleX()/2),stageBackGround.getCamera().position.y - GameConstants.screenHeight * 0.302f);

        imgResumeAlphaValTimer = 0;

        //timer
        lblTimerHeight = 450;
        lblTimer = new Label(""+(int)secondsToMatchEnd,new Skin(Gdx.files.internal(GameConstants.SKIN_GLASSY_RED)));
        lblTimer.setSize(lblTimer.getPrefWidth(),lblTimer.getPrefHeight());
        lblTimer.setPosition((Gdx.graphics.getWidth() - lblTimer.getWidth())/2,(Gdx.graphics.getHeight() - lblTimer.getHeight())/2 + lblTimerHeight);


    }//creo componenti (costruttore)


    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        // Clear the stuff that is left over from the previous render cycle
        Gdx.gl.glClearColor(1, 1, 1, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        //cameraController.update();

        super.render(delta);//triggera metodi di input controller

        //if(true){
        if(!USE_DEBUG_FOR_P1 || (!personaggio1.isAttacking() || (((Gdx.input.isKeyPressed(Input.Keys.Z) && DEBUG_FRAME_TIMER == DEBUG_SNSIBILITY_DELAY))))){//debug
        if (currentState == STATE_BATTLE) {

            //controllo collisioni (Collider2D posseduto da Attack posseduto da Character)
            for (int i = 0; i < existingColliders.size() - 1; i++) {
                for (int j = i + 1; j < existingColliders.size(); j++) {
                    //i tutti e 2 i collider sono attivi
                    if (existingColliders.get(i).absoluteOwner != existingColliders.get(j).absoluteOwner && existingColliders.get(i).isActive && existingColliders.get(j).isActive) {
                        if (existingColliders.get(i).isColliding(existingColliders.get(j))) {
                            existingColliders.get(i).collision(existingColliders.get(j));
                            existingColliders.get(j).collision(existingColliders.get(i));
                        }
                    }
                }
            }


            //eseguo input su personaggi
            if (personaggio1 != null) {//per precauzione
                personaggio1.controller.update(Gdx.graphics.getDeltaTime());
                personaggio1.executeInputs();
            }
            if (personaggio2 != null) {//per precauzione
                personaggio2.controller.update(Gdx.graphics.getDeltaTime());
                personaggio2.executeInputs();
            }

            //prima registro hit, poi le eseguo -> se i player si hittano in contemporanea entrambi si interrompono
            if (personaggio1 != null) {//per precauzione
                personaggio1.resolveHits();
                health1.setValue(health1.getMaxValue()*personaggio1.currentLife/personaggio1.maxLife);
                stamina1.setValue(health1.getMaxValue()*personaggio1.currentGuardAmount/personaggio1.maxLife);
            }
            if (personaggio2 != null) {//per precauzione
                personaggio2.resolveHits();
                health2.setValue(health2.getMaxValue()-health2.getMaxValue()*personaggio2.currentLife/personaggio2.maxLife);
                stamina2.setValue(stamina2.getMaxValue()-stamina2.getMaxValue()*personaggio2.currentGuardAmount/personaggio2.maxGuardAmount);
            }

            stageBackGround.act(Gdx.graphics.getDeltaTime());//esegue gli inputs di cose in stage
        }//funzionamento battaglia
            DEBUG_FRAME_TIMER = 0;
        }

        if(CAN_USE_DEBUG_FOR_P1 && DEBUG_FRAME_TIMER < DEBUG_SNSIBILITY_DELAY){
            DEBUG_FRAME_TIMER++;
        }//debug
        if(CAN_USE_DEBUG_FOR_P1 && DEBUG_ACTIVATION_FRAME_TIMER < DEBUG_SNSIBILITY_DELAY){
            DEBUG_ACTIVATION_FRAME_TIMER++;
        }//debug


        //disegno grafica 2D
        stageBackGround.draw();

        //disegno grafica 3D
        modelBatch.begin(camera3D);
        modelBatch.render(instances, environment);
        System.out.println(Character.AVAILABLE_PROJECTILE_MODELS[0].size());
        if(personaggio1 != null){
            for(Projectile projectile : personaggio1.existingCharacterProjectiles){
                if(projectile.model != null){
                    modelBatch.render(projectile.model, environment);
                }

            }
        }
        if(personaggio2 != null){
            for(Projectile projectile : personaggio2.existingCharacterProjectiles){
                if(projectile.model != null){
                    modelBatch.render(projectile.model, environment);
                }
            }
        }
        modelBatch.end();


        //disegno colliders (se settati a visibili)
        ShapeRenderer r = new ShapeRenderer();
        r.setAutoShapeType(true);
        r.begin(Filled);

        r.setColor(Collider2D.colorColliders);
        for( Collider2D col : existingColliders){
            col.draw(r);
        }

        r.end();



        //logica cambio stati
        if(personaggio1.currentLife == 0){
            currentState = STATE_WIN_P2;
        }//vittoria p2
        if(personaggio2.currentLife == 0){
            currentState = STATE_WIN_P1;
        }//vittoria p1
        if(secondsToMatchEnd <= 0){
            currentState = STATE_DAW;
        }//pareggio



        //grafica stati

        stageBackGroundBatch.begin();
        if(currentState == STATE_PAUSE){
            //System.out.println("stato pausa");
            imgPause.draw(stageBackGroundBatch,1);
            imgResume.draw(stageBackGroundBatch,0.5f + (float)((Math.cos(imgResumeAlphaValTimer) + 1)/2*0.5f));

            imgResumeAlphaValTimer = (float)((imgResumeAlphaValTimer + delta * imgResumeAlphaValMultiplier)%(2*Math.PI));
        }
        if(currentState == STATE_DAW){
            //System.out.println("stato pareggio");
            imgDraw.draw(stageBackGroundBatch,1);
        }
        if(currentState == STATE_WIN_P1){
            //System.out.println("stato vittoria");
            imgP1Win.draw(stageBackGroundBatch,1);

        }
        if(currentState == STATE_WIN_P2){
            //System.out.println("stato vittoria");
            imgP2Win.draw(stageBackGroundBatch,1);

        }
        if(currentState == STATE_BATTLE){

            secondsToMatchEnd -= delta;

            if(secondsToMatchEnd < 0){
                secondsToMatchEnd = 0;
            }
        }
        lblTimer.setText(""+(int)secondsToMatchEnd);
        lblTimer.setSize(lblTimer.getPrefWidth(),lblTimer.getPrefHeight());
        lblTimer.setPosition((Gdx.graphics.getWidth() - lblTimer.getWidth())/2,(Gdx.graphics.getHeight() - lblTimer.getHeight())/2 + lblTimerHeight);
        lblTimer.draw(stageBackGroundBatch, 1);
        stageBackGroundBatch.end();

        if(framesPassedByLastStartPress < START_BUTTON_DELAY){
            framesPassedByLastStartPress++;
        }//aumento contatore per delay start



    }

    private void PressStart(){
        if(framesPassedByLastStartPress == START_BUTTON_DELAY){

            framesPassedByLastStartPress = 0;
            if(currentState == STATE_BATTLE){
                currentState = STATE_PAUSE;
                imgResumeAlphaValTimer = 0;
            }else{
                if(currentState == STATE_PAUSE){
                    currentState = STATE_BATTLE;
                }else{
                    if(currentState == STATE_DAW || currentState == STATE_WIN_P1 || currentState == STATE_WIN_P2){
                        game.setScreen(new ChooseCharactersScreen(game));
                        this.dispose();
                    }
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {

        //2D  al momento la camera rimane sempre invariata -> mostra sempre tutto, non rispetta proporzioni
        //stage.getCamera().position.set((float) width/2,(float) height/2,0);
        //stage.getCamera().update();


        //3D
        camera3D.viewportWidth=width;
        camera3D.viewportHeight=height;
        camera3D.update();



        //System.out.println("viewport screen size "+viewPort.getScreenWidth()+"    "+viewPort.getScreenHeight());
        //System.out.println("viewport world size "+viewPort.getWorldWidth()+"    "+viewPort.getWorldHeight());
        //System.out.println("camera position"+viewPort.getCamera().position.toString());
        //System.out.println("viewport camera size "+viewPort.getCamera().viewportWidth+"    "+viewPort.getCamera().viewportHeight);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        //stageBackGround.dispose();
    }



    @Override
    public void c1_buttonDpadRight() {
        if(currentState == STATE_BATTLE){
            personaggio1.setMoveLeft(true);
        }


    }
    public void c1_buttonDpadLeft() {
        if(currentState == STATE_BATTLE){
            personaggio1.setMoveRight(true);
        }


    }
    public void c1_buttonDpadUp() {
        //System.out.println("action up");

    }
    public void c1_buttonDpadDown() {
        if(currentState == STATE_BATTLE){
            personaggio1.crouching=true;
        }

    }
    public void c1_buttonA() {
        if(currentState == STATE_BATTLE){
            personaggio1.tryToJump();
        }

    }
    public void c1_buttonB() {
        if(currentState == STATE_BATTLE){
            personaggio1.setAttack(Character.ATTACK_BUTTON_B);
        }

    }
    public void c1_buttonBack() {
        if(CAN_USE_DEBUG_FOR_P1 && DEBUG_ACTIVATION_FRAME_TIMER == DEBUG_SNSIBILITY_DELAY){
            DEBUG_ACTIVATION_FRAME_TIMER = 0;
            USE_DEBUG_FOR_P1 = !USE_DEBUG_FOR_P1;
        }//debug frame per frame
        if(currentState == STATE_PAUSE){
            game.setScreen(new MenuScreen(game));
            this.dispose();
        }

    }
    public void c1_buttonY() {
        if (currentState == STATE_BATTLE){
            personaggio1.setAttack(Character.ATTACK_BUTTON_Y);
        }

    }
    public void c1_buttonX() {
        if(currentState == STATE_BATTLE){
            personaggio1.setAttack(Character.ATTACK_BUTTON_X);
        }

    }
    public void c1_buttonStart() {
        PressStart();

    }
    public void c1_buttonL1() {
        if(currentState == STATE_BATTLE){
            personaggio1.tryingToGuard = true;
        }

    }
    public void c1_buttonR1() {
        if(currentState == STATE_BATTLE){
            personaggio1.tryingToGuard = true;
        }

    }
    public void c1_buttonLeftStick() {
        //System.out.println("buttonLStick");

    }
    public void c1_buttonRightStick() {
        //System.out.println("buttonRStick");

    }
    public void c1_axisLeftX(float val) {
        if(currentState == STATE_BATTLE) {
            if (val > 0) {
                c1_buttonDpadRight();
            } else {
                c1_buttonDpadLeft();
            }
        }

    }
    public void c1_axisRightX(float val) {
        //System.out.println("axisRightX");

    }
    public void c1_axisLeftY(float val) {
        if(currentState == STATE_BATTLE) {
            if (val > 0) {
                c1_buttonDpadDown();
            } else {
                c1_buttonDpadUp();
            }
        }

    }
    public void c1_axisRightY(float val) {
        //System.out.println("axisRightY");

    }


    public void c2_buttonDpadRight() {
        if(currentState == STATE_BATTLE) {
            personaggio2.setMoveLeft(true);
        }

    }
    public void c2_buttonDpadLeft() {
        if(currentState == STATE_BATTLE){
            personaggio2.setMoveRight(true);
        }


    }
    public void c2_buttonDpadUp() {

    }
    public void c2_buttonDpadDown() {
        if(currentState == STATE_BATTLE){
            personaggio2.crouching=true;
        }


    }
    public void c2_buttonA() {
        if(currentState == STATE_BATTLE){
            personaggio2.tryToJump();
        }


    }
    public void c2_buttonB() {
        if(currentState == STATE_BATTLE){
            personaggio2.setAttack(Character.ATTACK_BUTTON_B);
        }


    }
    public void c2_buttonBack() {
        if(currentState == STATE_PAUSE){
            game.setScreen(new MenuScreen(game));
            this.dispose();
        }

    }
    public void c2_buttonY() {
        if(currentState == STATE_BATTLE){
            personaggio2.setAttack(Character.ATTACK_BUTTON_Y);
        }


    }
    public void c2_buttonX() {
        if(currentState == STATE_BATTLE){
            personaggio2.setAttack(Character.ATTACK_BUTTON_X);
        }


    }
    public void c2_buttonStart() {
            PressStart();

    }
    public void c2_buttonL1() {
        if(currentState == STATE_BATTLE){
            personaggio2.tryingToGuard = true;
        }


    }
    public void c2_buttonR1() {
        if(currentState == STATE_BATTLE){
            personaggio2.tryingToGuard = true;
        }


    }
    public void c2_buttonLeftStick() {

    }
    public void c2_buttonRightStick() {

    }
    public void c2_axisLeftX(float val) {
        //System.out.println("axisLeftX");
        if(currentState == STATE_BATTLE){
            if(val>0){
                c2_buttonDpadRight();
            }else{
                c2_buttonDpadLeft();
            }
        }


    }
    public void c2_axisRightX(float val) {

    }
    public void c2_axisLeftY(float val) {

    }
    public void c2_axisRightY(float val) {

    }

}













