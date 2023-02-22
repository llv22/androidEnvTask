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

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.gitlab.ardash.appleflinger.AppleflingerGame;
import com.gitlab.ardash.appleflinger.global.Assets;
import com.gitlab.ardash.appleflinger.global.GameManager;
import com.gitlab.ardash.appleflinger.global.GameState;
import com.gitlab.ardash.appleflinger.helpers.Pref;
import com.gitlab.ardash.appleflinger.i18n.I18N;
import com.gitlab.ardash.appleflinger.missions.Mission;

public class MissionSelectScreen extends GenericScreen {
	private static int selectedEpisode=0;
	   
	public MissionSelectScreen() {

	}
    
    @Override
    public void show() {
    	super.show();
		gm.setGameState(GameState.MISSION_SELECT);

		if (AppleflingerGame.RL_TASK.isEnabled()) {
			Mission mission = Mission.valueOf(AppleflingerGame.RL_TASK.get("mission","M_1_1"));
			gm.resetAll(mission);
			gm.setScreen(mission);
		}
    }
    
    @Override
    protected void buildGameGUI() {
    	super.buildGameGUI();
    	
        Table table = new Table();
        table.setFillParent(true);
        
        // make user select episode if none is selected yet
        if (selectedEpisode == 0)
        {
        	// btn for each episode (episode selection screen)
        	for (final Integer episode : Mission.getAvailableEpisodes()) {
        		Assets.SpriteAsset.BTN_FL_EMPTY.get().setSize(200, 200);
    	        final LabelSpriteButton episodeBtn = new LabelSpriteButton(Assets.SpriteAsset.BTN_FL_EMPTY.get(),
    	        		getEpisodeName(episode));
    	        table.add(episodeBtn);
    	        episodeBtn.addListener(new ClickListener() {
    	        	@Override
    	        	public void clicked(InputEvent event, float x, float y) {
    	        		super.clicked(event, x, y);
    	        		selectedEpisode = episode;
    	        		show();
    	        	}
    	        });
			}
        }
        else
        {
            // label for name of episode
            Label lblEpiName = new Label(getEpisodeName(selectedEpisode), menustyle);   
    		lblEpiName.setPosition(0, SCREEN_HEIGHT - SCREEN_HEIGHT/3);  
    		lblEpiName.setWidth(SCREEN_WIDTH);
    		lblEpiName.setAlignment(Align.center);
    		
            guiStage.addActor(lblEpiName);
	        int i=0;
	    	for (final Mission mission : Mission.values())
	    	{
	    		//button for each missing in the selected episode
	    		if (mission.getMajor() == selectedEpisode)
	    		{
			        final LevelButton btnMission = new LevelButton(mission.getMinor());
			        btnMission.moveBy(SCREEN_WIDTH/2-100, SCREEN_HEIGHT/2-100);
		            table.add(btnMission).width(150).height(150);
		            
		            if (!GameManager.ALLLEVELS) // if not debugging, disable missions properly
		            	btnMission.setDisabled(!Pref.isMissionActivated(mission));
		            
		            btnMission.addListener(new ClickListener() {
		    			@Override
		    			public void clicked(InputEvent event, float x, float y) {
			            	if (btnMission.isDisabled())
			            		return;
		    				super.clicked(event, x, y);
		    				gm.resetAll(mission);
		    				gm.setScreen(mission);
		    			}
		    		});
	
		            if (++i%9==0)
		            	table.row();
	    		}
	    	}
        }
        guiStage.addActor(table);
        
        // message label below
        Label labelMessage = new Label(I18N.getString("chooseALevel"), menustyle);   
		labelMessage.setPosition(0, SCREEN_HEIGHT/4);  
		labelMessage.setWidth(SCREEN_WIDTH);
		labelMessage.setAlignment(Align.center);
        guiStage.addActor(labelMessage);
        
        //back btn
        SpriteButton btnBack = new SpriteButton(Assets.SpriteAsset.BTN_BACK.get());
        btnBack.moveBy(SCREEN_WIDTH-100, SCREEN_HEIGHT-100);
        guiStage.addActor(btnBack);
        btnBack.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				if (selectedEpisode == 0)
				{
					gm.setScreen(new MainMenuScreen());
				}
				else
				{
					selectedEpisode = 0;
					show();
				}
			}
		});

        linkHardwareBackButtonToButton(btnBack);
    }

	private static String getEpisodeName(Integer episode) {
		switch (episode) {
		case 1:
			return I18N.getString("original");
		case 2:
			return I18N.getString("winter");
		default:
			throw new RuntimeException("No episode name found for episode number "+episode);
		}
	}
    
    public static void setSelectedEpisode(int selectedEpisode) 
    {
		MissionSelectScreen.selectedEpisode = selectedEpisode;
	}


    
}  
