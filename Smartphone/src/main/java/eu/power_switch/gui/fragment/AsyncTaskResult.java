/*
 *  PowerSwitch by Max Rosin & Markus Ressel
 *  Copyright (C) 2015  Markus Ressel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.gui.fragment;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Markus on 19.03.2016.
 */
public class AsyncTaskResult<T> {

    private Exception exception;
    private List<T>   elements;

    public AsyncTaskResult(Exception e) {
        this.exception = e;
        this.elements = null;
    }

    public AsyncTaskResult(T... elements) {
        this.exception = null;
        this.elements = Arrays.asList(elements);
    }

    public boolean isSuccess() {
        if (exception != null) {
            return false;
        }

        if (elements == null) {
            return false;
        }

        if (elements.size() == 1) {
            if (elements.get(0) == null) {
                return false;
            }
        }

        return true;
    }

    public Exception getException() {
        return exception;
    }

    public List<T> getResult() {
        return elements;
    }
}
