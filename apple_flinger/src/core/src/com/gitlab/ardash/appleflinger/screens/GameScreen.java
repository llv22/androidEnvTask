/*******************************************************************************
 * Copyright (C) 2015-2018 Andreas Redmer <ar-appleflinger@abga.be>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.gitlab.ardash.appleflinger.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gitlab.ardash.appleflinger.AppleflingerGame;
import com.gitlab.ardash.appleflinger.GameRenderer;
import com.gitlab.ardash.appleflinger.GameWorld;
import com.gitlab.ardash.appleflinger.global.Assets;
import com.gitlab.ardash.appleflinger.global.Assets.LabelStyleAsset;
import com.gitlab.ardash.appleflinger.global.Assets.MusicAsset;
import com.gitlab.ardash.appleflinger.global.Assets.SoundAsset;
import com.gitlab.ardash.appleflinger.global.GameManager;
import com.gitlab.ardash.appleflinger.global.GameManager.OnCurrentPlayerChangeListener;
import com.gitlab.ardash.appleflinger.global.GameState;
import com.gitlab.ardash.appleflinger.helpers.Achievement;
import com.gitlab.ardash.appleflinger.helpers.BackButtonAdapter;
import com.gitlab.ardash.appleflinger.helpers.Clicker;
import com.gitlab.ardash.appleflinger.helpers.Pref;
import com.gitlab.ardash.appleflinger.helpers.SoundPlayer;
import com.gitlab.ardash.appleflinger.i18n.I18N;
import com.gitlab.ardash.appleflinger.listeners.OnGameOverListener;
import com.gitlab.ardash.appleflinger.listeners.OnPointsChangeListener;
import com.gitlab.ardash.appleflinger.listeners.OnUnlockAchievementListener;
import com.gitlab.ardash.appleflinger.missions.Mission;

public class GameScreen implements Screen {  
	   
    // this is actually my tablet resolution in landscape mode. I'm using it for making the GUI pixel-exact.  
//    public static float SCREEN_WIDTH = 1024;  
//    public static float SCREEN_HEIGHT = 600;  
    
    public static float SCREEN_WIDTH = 1920;  
    public static float SCREEN_HEIGHT = 1080;
    
    private GameWorld world; // contains the game world's bodies and actors.  
    private GameRenderer renderer; // our custom game renderer.  
    private Stage guiStage; // stage that holds the GUI. Pixel-exact size.  
    private OrthographicCamera guiCam; // camera for the GUI. It's the stage default camera.  
    
    public final Mission mission;
	private Label labelMessage;
	private Label labelTime;
	private PauseScreenActor pauseScreen = new PauseScreenActor();
	private boolean isAnnouncementFrozen= false;
	private Label labelAllPointsP1;
	private Label labelAllPointsP2;
	private float notPausedTimeOnScreen = 0;
    
    public GameScreen(Mission mission) {
		this.mission = mission;
	}

	@Override  
    public final void show() {
		// preload assets (otherwise sounds play too fast after loading, so they are silenced the first time)
		// if there is  no assync loading done before, this will cause a delay and button stays down a while
		//MAssets.load(); // just to be sure, in case something was skipped by assync loading
        final GameManager gm = GameManager.getInstance();
		gm.setGameState(GameState.LOADING_SCREEN);
		SoundPlayer.pauseMusic(Assets.getMusic(MusicAsset.BG));

        //this.stage = new Stage(); // create the GUI stage
        //this.stage.setViewport(SCREEN_WIDTH, SCREEN_HEIGHT, false); // set the GUI stage viewport to the pixel size
        //this.stage = new Stage(new ExtendViewport(SCREEN_WIDTH, SCREEN_HEIGHT, SCREEN_WIDTH, SCREEN_HEIGHT));
        //this.stage = new Stage(new StretchViewport(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.guiStage = new Stage(new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT));

        world = new GameWorld(mission);
        renderer = new GameRenderer(world);
        
        // until further notice the game actor will get all the events (game-over reverts this)
        gm.getInputMultiplexer().clear();
        gm.getInputMultiplexer().addProcessor(guiStage);
        gm.getInputMultiplexer().addProcessor(world.stage);
        
        // register with game manger
        gm.currentGameScreen=this;
        
        // add GUI actors to stage, labels, meters, buttons etc.  
        buildGameGUI();
        setAnnouncementText(I18N.getString("pleaseWait")+" ...");
    }

	private static Label createMiniLabel(CharSequence text)
	{
        final LabelStyle ministyle = new LabelStyle(LabelStyleAsset.MINILABEL.style);
//		ministyle.font = Assets.FontAsset.FLINGER_03_B2_DIAG_MINIL.font;
		ministyle.fontColor = new Color(1, 1, 1, 0.8f);
		final Label labelPointsTop = new Label(text, ministyle);
		labelPointsTop.setTouchable(Touchable.disabled);
		return labelPointsTop;
	}
	
	/**
	 * 
	 */
	private void buildGameGUI() {
        final GameManager gm = GameManager.getInstance();
        LabelStyle labelstyle = Assets.LabelStyleAsset.BIGMENUSTYLE.style;
//        float lineheight = labelstyle.font.getAscent()+labelstyle.font.getDescent()+labelstyle.font.getCapHeight()+labelstyle.font.getLineHeight();
        float lineheight = labelstyle.font.getLineHeight()+26;
        
        // these are some labels in the toptable, they get some updates in listeners below
        labelAllPointsP1 = createMiniLabel(gm.PLAYER1.getAllPoints()+""); 
        labelAllPointsP2 = createMiniLabel(gm.PLAYER2.getAllPoints()+""); 
        final Label labelWinsP1 = createMiniLabel(gm.PLAYER1.getWins()+""); 
        final Label labelWinsP2 = createMiniLabel(gm.PLAYER2.getWins()+""); 

        labelMessage = new Label("A", labelstyle);
//        labelMessage.setWrap(true);
        labelMessage.setPosition(0, SCREEN_HEIGHT-lineheight*4);  
        labelMessage.setWidth(SCREEN_WIDTH);  
        labelMessage.setAlignment(Align.top);  
        labelMessage.setTouchable(Touchable.disabled);
        guiStage.addActor(labelMessage);

        // label to show expired game time
        labelTime = createMiniLabel("00:00");
        labelTime.setWidth(SCREEN_WIDTH);
        labelTime.setHeight(SCREEN_HEIGHT);
        labelTime.setAlignment(Align.topRight);
        labelTime.setTouchable(Touchable.disabled);
        guiStage.addActor(labelTime);
        
        final Label labelNameP1 = new Label(".", labelstyle);   
		labelNameP1.setPosition(0, SCREEN_HEIGHT-lineheight*2);  
        labelNameP1.setAlignment(Align.left);  
		labelNameP1.setTouchable(Touchable.disabled);
        guiStage.addActor(labelNameP1);  

        final Label labelNameP2 = new Label(".", labelstyle);   
		labelNameP2.setPosition(0, SCREEN_HEIGHT-lineheight*2);  
		labelNameP2.setWidth(SCREEN_WIDTH);  
		labelNameP2.setAlignment(Align.right);  
		labelNameP2.setTouchable(Touchable.disabled);
        guiStage.addActor(labelNameP2);  
        
        final Label labelPointsP1 = new Label(" "+I18N.getString("POINTS")+":  0 ", labelstyle);     
		labelPointsP1.setPosition(0, SCREEN_HEIGHT-lineheight*3f);  
		labelPointsP1.setTouchable(Touchable.disabled);
        guiStage.addActor(labelPointsP1);  
        
        final Label labelPointsP2 = new Label(" "+I18N.getString("POINTS")+":  0  ", labelstyle);     
		labelPointsP2.setPosition(0, SCREEN_HEIGHT-lineheight*3f);  
        labelPointsP2.setWidth(SCREEN_WIDTH);  
        labelPointsP2.setAlignment(Align.right);  
        labelPointsP2.setTouchable(Touchable.disabled);
        guiStage.addActor(labelPointsP2); 
        
        // set listeners for points change
        gm.PLAYER1.setOnPointsChangeListener(new OnPointsChangeListener() {
			@Override
			public void onPointChange() {
				labelPointsP1.setText(String.format(" "+I18N.getString("POINTS")+":  %d ", gm.PLAYER1.getPoints()));  
				labelAllPointsP1.setText((gm.PLAYER1.getPoints()+gm.PLAYER1.getAllPoints())+"");
				adjustPointLabelColors(labelPointsP1,labelPointsP2,gm.PLAYER1.getPoints(),gm.PLAYER2.getPoints());
				adjustPointLabelColors(labelAllPointsP1,labelAllPointsP2,
						gm.PLAYER1.getPoints()+gm.PLAYER1.getAllPoints(),gm.PLAYER2.getPoints()+gm.PLAYER2.getAllPoints());
			}
		});
        gm.PLAYER2.setOnPointsChangeListener(new OnPointsChangeListener() {
			@Override
			public void onPointChange() {
				labelPointsP2.setText(String.format(" "+I18N.getString("POINTS")+":  %d  ", gm.PLAYER2.getPoints()));
				labelAllPointsP2.setText((gm.PLAYER2.getPoints()+gm.PLAYER2.getAllPoints())+"");
				adjustPointLabelColors(labelPointsP1,labelPointsP2,gm.PLAYER1.getPoints(),gm.PLAYER2.getPoints());
				adjustPointLabelColors(labelAllPointsP1,labelAllPointsP2,
						gm.PLAYER1.getPoints()+gm.PLAYER1.getAllPoints(),gm.PLAYER2.getPoints()+gm.PLAYER2.getAllPoints());
			}
		});
        
        // set game over listener
        final Screen screenToBeDisposed = this;
        gm.setOnGameOverListener(new OnGameOverListener() {
			@Override
			public void onGameOver() {
				// GUI catches all events from now, game not playable anymore
				gm.getInputMultiplexer().removeProcessor(world.stage);
				setAnnouncementText(String.format(I18N.getString("gameOver")+"\n%s "+I18N.getString("won")+"\n"+I18N.getString("withDPoints")+"\n"+I18N.getString("touchScreenToContinue")+".", gm.winner.getName(), gm.winner.getPoints()));
				freezeAnnouncementText();
				
				// center colour
				labelMessage.addAction(Actions.color(Color.GREEN, 1));
				
				// winner label colours, set colour by using 0 or 1 or 2 points
				final int windicator = gm.winner == gm.PLAYER1 ? 0 : 2;
				adjustPointLabelColors(labelPointsP1,labelPointsP2,1,windicator);
				adjustPointLabelColors(labelAllPointsP1,labelAllPointsP2,1,windicator);
				adjustPointLabelColors(labelNameP1,labelNameP2,1,windicator);
				adjustPointLabelColors(labelWinsP1,labelWinsP2,1,windicator);
				
				// update the wins-label as well, they are correct on next screen load,
				// but it looks bad to have them not incremented here.
				Label winsLabel = gm.winner == gm.PLAYER1 ? labelWinsP1 : labelWinsP2;
				winsLabel.setText("" + (Integer.parseInt(winsLabel.getText().toString()) +1));

				gm.setGameState(GameState.GAME_OVER_SCREEN);
				SoundPlayer.playMusic(Assets.getMusic(MusicAsset.BG));
				
        		final Mission nextmission;
        		
				if (gm.isPlayer2CPU())
				{
					if (gm.winner == gm.PLAYER1)
					{
						nextmission = mission.getNext();
					}
					else
					{
						nextmission = mission;
					}
					
	        		// if it was played against computer it unlocks next missions
					Pref.setMissionActivated(nextmission,true);
				}
				else
				{
					// playing against human so else, doesn't unlock anything, but it goes over to the next level in any case
					nextmission = mission.getNext();
				}
        		


		        // the only way the gui stage can now receive an event is: GAME OVER
		        guiStage.addListener(new InputListener() {
		        	
		        	@Override
		        	public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
		        		return true;
		        	}
		        	@Override
		        	public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
		        		super.touchUp(event, x, y, pointer, button);
						if (AppleflingerGame.RL_TASK.isEnabled()) {
							return; // do nothing
						}
		        		gm.resetRound(nextmission);
						gm.setScreen(nextmission);
		        		screenToBeDisposed.dispose();
		        	}
		        });

			}
		});
        
        
    	// set now because names don't change during game
		gm.PLAYER1.setName(Pref.getPlayer1name());
   		labelNameP1.setText(String.format(" %s ", gm.PLAYER1.getName())); 
   		
   		if (gm.isPlayer2CPU())
   		{
   			gm.PLAYER2.setName(I18N.getString("computer")); 
   		}
   		else
   		{
   			gm.PLAYER2.setName(Pref.getPlayer2name());
   		}
   		labelNameP2.setText(String.format(" %s  ", gm.PLAYER2.getName())); 
   		
   		// register listener for gamestateChanges
   		gm.setOnCurrentPlayerChangeListener(new OnCurrentPlayerChangeListener() {
			@Override
			public void onCurrentPlayerChange() {
		        setAnnouncementText(String.format("%s\n"+I18N.getString("itIsYourTurn"), gm.currentPlayer.getName()));   
			}
		});
   		
   		// pause button
        final SpriteButton btnPause = new SpriteButton(Assets.SpriteAsset.BTN_PAUSE.get());
        btnPause.moveBy(0+100, SCREEN_HEIGHT-100);
        //guiStage.addActor(btnPause);
        btnPause.addListener(new ClickListener(){@Override
        public void clicked(InputEvent event, float x, float y) {
			if (AppleflingerGame.RL_TASK.isEnabled()) {
				return; // do nothing
			}
			guiStage.addActor(pauseScreen);
			gm.setPaused(true);
        	super.clicked(event, x, y);
        }});
        
        // handle hardware button
        gm.getInputMultiplexer().addProcessor(new BackButtonAdapter() {
			@Override
			public boolean handleBackButton() {
				if (AppleflingerGame.RL_TASK.isEnabled()) {
					return true; // do nothing
				}
				Clicker.click(btnPause);
				return true;
			}
		});
        
        // sound button
        final SpriteButton btnSound = new SpriteButton(Assets.SpriteAsset.BTN_SOUND_ON.get(),Assets.SpriteAsset.BTN_SOUND_OFF.get());
        btnSound.moveBy(0, SCREEN_HEIGHT-100);
        btnSound.setCheckable(true);
		btnSound.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				if (AppleflingerGame.RL_TASK.isEnabled()) {
					return; // do nothing
				}
				Pref.setSoundOn(!btnSound.isChecked());
			}
		});
        btnSound.setChecked(!Pref.getSoundOn());

        final Table miniStatsTable = new Table();
        miniStatsTable.setTouchable(Touchable.disabled);
        miniStatsTable.add(labelAllPointsP1).right().top();
        miniStatsTable.add(createMiniLabel(I18N.getString("points"))).padLeft(20).padRight(20);
        miniStatsTable.add(labelAllPointsP2).left();
		miniStatsTable.add().width(GameWorld.UNIT_WIDTH/2);
		miniStatsTable.row();
		miniStatsTable.add(labelWinsP1).right();
        miniStatsTable.add(createMiniLabel(I18N.getString("wins"))).padLeft(20).padRight(20);
		miniStatsTable.add(labelWinsP2).left();
		miniStatsTable.add().width(GameWorld.UNIT_WIDTH/2);
        miniStatsTable.row();
        miniStatsTable.add();
        miniStatsTable.add(createMiniLabel(I18N.getString("level"))).padLeft(20).padRight(20);
        miniStatsTable.add(createMiniLabel(mission.getMinor()+"")).left();
		miniStatsTable.add().width(GameWorld.UNIT_WIDTH/2);

        final Table topLeftTable = new Table();
        topLeftTable.setFillParent(true);
        topLeftTable.align(Align.topLeft);
        topLeftTable.add(btnSound).padLeft(10);
        topLeftTable.add(btnPause).padLeft(10);

        topLeftTable.add(miniStatsTable).width(SCREEN_WIDTH - 4*(10+btnPause.getWidth())).padLeft(0).center();
        guiStage.addActor(topLeftTable);
        
        // register achievement unlocked listener to drop the popup in time
        gm.setOnUlockAchievementListener(new OnUnlockAchievementListener() {
			@Override
			public void onUnlockAchievement(Achievement a) {
				spawnAchievementPopup(a);
			}
		});
        
        // TODO add more other GUI elements here  
	}
	
	/**
	 * Adjust the colours of 2 labels according to the points in the input parameters.
	 * If p1 is higher then labelPointsP1, will be converted to green. otherwise 
	 * labelPointsP2 will become green. The other label will be set to white.
	 * @param labelPointsP1
	 * @param labelPointsP2
	 * @param p1
	 * @param p2
	 */
	private static void adjustPointLabelColors(Label labelPointsP1, Label labelPointsP2, int p1, int p2) {
		Color cL1 = Color.WHITE;
		Color cL2 = Color.WHITE;
		
		if (p1 > p2)
			cL1 = Color.GREEN;
		if (p1 < p2)
			cL2 = Color.GREEN;
		
		labelPointsP1.setColor(cL1);
		labelPointsP2.setColor(cL2);
	}

      
    @Override  
    public void render(float delta) {  
    	
        guiCam = (OrthographicCamera) guiStage.getCamera();  
        guiCam.position.set(SCREEN_WIDTH/2, SCREEN_HEIGHT/2, 0);  
  
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);  
        Gdx.gl.glEnable(GL20.GL_TEXTURE_2D);  
        guiCam.update();  
  
        // dont update the world, if game paused
        if (!GameManager.getInstance().isPaused())
        {
			world.update(delta); // update the box2d world
			notPausedTimeOnScreen  += delta;
			updateTimeOnScreen(notPausedTimeOnScreen);
        }
        guiStage.act(delta); // update GUI
          
        renderer.render(); // draw the box2d world
        guiStage.setDebugAll(GameManager.DEBUG);
        guiStage.draw(); // draw the GUI
    }

	@Override
	public void resize(int width, int height) {
		// pass true because camera is unchanged on this UI stage
		guiStage.getViewport().update(width, height, true);
		
		// make the actors and stage still react to touch after resize, but coordinates are still fucked after resize
		// taken out because every click refreshes the handler now
//		Gdx.input.setInputProcessor(world.stage);
	}
	
	public void setAnnouncementText(CharSequence text)
	{
		setAnnouncementText(text, false);
	}
	
	public void setAnnouncementText(CharSequence text, boolean silent)
	{
		if (isAnnouncementFrozen)
			return;
		
		labelMessage.setText(text+"\n"); 
		
		if (!silent)
			SoundPlayer.playSound(Assets.getSound(SoundAsset.NOTIFICATION));
		
		// if it is the final message freeze it so it can't be overwritten anymore
		// TODO it also should not disappear anymore
//		GameManager gm = GameManager.getInstance();
//		if (gm.winner != gm.NONE)
//		{
//			freezeAnnouncementText();
//		}
	}

	private void freezeAnnouncementText() {
		//labelMessage.setPosition(0, SCREEN_HEIGHT/2);
		isAnnouncementFrozen = true;
	}

	public void spawnAchievementPopup(Achievement a) {
		final Sprite sprite = Assets.SpriteAsset.BTN_UNLOCKED.get();
        final String luText = I18N.getString("unlocked");
		final String labelText = I18N.getString(a.getNameId())+"\n"+luText;
		final LabelSpriteButton achBtn = new LabelSpriteButton(sprite, labelText, AchievementsScreen.ACH_TXT_WIDTH);
		achBtn.setTouchable(Touchable.disabled);
		Group grp = new Group();
		Image img = new Image(sprite);
		img.setSize(390, 390);
		grp.addActor(img);
		final Label label = achBtn.getLabel();
		grp.addActor(label);
		label.setAlignment(Align.center);
		label.setSize(390, 390);
		guiStage.addActor(grp);
		grp.setPosition(GameScreen.SCREEN_WIDTH/2 - img.getWidth()/2, GameScreen.SCREEN_HEIGHT);
		grp.setTouchable(Touchable.disabled);
		
		// bounce in and fade+bounce out
		Action a1 = Actions.moveBy(0, -SCREEN_HEIGHT, 5, Interpolation.bounceOut);
		Action a2 = Actions.parallel(Actions.fadeOut(5), Actions.moveBy(0, -390,5,Interpolation.elasticIn));
		Action action = Actions.sequence(a1,Actions.delay(1),a2, Actions.removeActor(grp));
		grp.addAction(action );
		SoundPlayer.playSound(Assets.getSound(SoundAsset.BELL));
	}
	
	public void updateTimeOnScreen(float playTime) {
		
		// if game is already over, no nee to update that label any more
		if (isAnnouncementFrozen)
			return;
		
		final int totalSecs = MathUtils.round(playTime);
		final int hours = totalSecs / 3600;
		final int minutes = (totalSecs % 3600) / 60;
		final int seconds = totalSecs % 60;

		final String timeString;
		if (hours == 0)
			timeString = String.format("%02d:%02d", minutes, seconds);
		else
		{
			timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
		}
		labelTime.setText(timeString);
	}

	public Stage getGuiStage() {
		return guiStage;
	}

	public OrthographicCamera getGuiCam() {
		return guiCam;
	}

	public GameRenderer getRenderer() {
		return renderer;
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		// TODO with the current implementation a screen should be disposed after hiding (maybe not on pausing?)
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
}  
