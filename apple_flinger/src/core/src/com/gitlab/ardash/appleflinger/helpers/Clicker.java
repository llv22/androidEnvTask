/*******************************************************************************
 * Copyright (C) 2018 Andreas Redmer <ar-appleflinger@abga.be>
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
package com.gitlab.ardash.appleflinger.helpers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

public class Clicker {
	public static void click(Actor elementToClickon) {
		InputEvent event1 = new InputEvent();
		event1.setType(InputEvent.Type.touchDown);
		elementToClickon.fire(event1);

		InputEvent event2 = new InputEvent();
		event2.setType(InputEvent.Type.touchUp);
		elementToClickon.fire(event2);
	}
}
