/*******************************************************************************
 * Copyright (C) 2017-2018 Andreas Redmer <ar-appleflinger@abga.be>
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
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.gitlab.ardash.appleflinger.AppleflingerGame;
import com.gitlab.ardash.appleflinger.global.Assets;
import com.gitlab.ardash.appleflinger.global.Assets.TextureAsset;
import com.gitlab.ardash.appleflinger.screens.GenericScreen;
import com.gitlab.ardash.appleflinger.screens.LabelSpriteButton;

public class AdvancedDialog extends Dialog {

	protected final Sprite EMPTY_TEX;
	protected final Image backgrPixel;

	public AdvancedDialog() {
		super("", new WindowStyle( 
				Assets.FontAsset.FLINGER_03_B2_DIAG_MINIL.font,
				Color.WHITE,
				new TextureRegionDrawable(new TextureRegion(Assets.getTexture(Assets.TextureAsset.LARGE_DIALOG)))
				));
		setModal(true);
		setMovable(false);
		setResizable(false);

        EMPTY_TEX = Assets.SpriteAsset.BTN_SQ_EMPTY.get();
		if (AppleflingerGame.RL_TASK.get("simple", false)) {
			backgrPixel = new Image();
		} else {
			//noinspection ConstantConditions
			backgrPixel = new Image(Assets.getTexture(TextureAsset.BACKGR));
		}
		backgrPixel.setSize(GenericScreen.SCREEN_WIDTH, GenericScreen.SCREEN_HEIGHT);
		if (AppleflingerGame.RL_TASK.get("simple", false)) {
			backgrPixel.setColor(Color.WHITE);
		} else {
			backgrPixel.setColor(0, 0, 0, 0.8f);
		}
        
		getButtonTable().addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				hide();
			}
		});

	}

	protected void button(LabelSpriteButton btnCancel) {
		getButtonTable().add(btnCancel);
		setObject(btnCancel, "");
	}
	
	public Dialog text(String text, LabelStyle labelStyle, float wordWrapWidth) {
		//final Dialog ret = text(text, labelStyle);
		Label label = new Label(text, labelStyle);
		label.setWrap(true);
		label.setAlignment(Align.center, Align.center) ;
		label.setWidth(wordWrapWidth);
		contentTable.add(label).width(wordWrapWidth).center();
		return this;
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		backgrPixel.draw(batch, parentAlpha); // semi black background
		super.draw(batch, parentAlpha);
	}

}