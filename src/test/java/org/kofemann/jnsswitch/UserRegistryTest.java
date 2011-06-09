package org.kofemann.jnsswitch;
/*
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program (see the file COPYING.LIB for more
 * details); if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserRegistryTest {

    private static final String ROOT_NAME = "root";
    private static final int ROOT_ID = 0;

    private UserRegistry registry;

    @Before
    public void setUp() {
        registry = new UserRegistry();
    }
    @Test
    public void testRootByName() {
        assertEquals(ROOT_ID, registry.uidByName(ROOT_NAME));
    }

    @Test
    public void testRootById() {
        assertEquals(ROOT_NAME, registry.userById(ROOT_ID));
    }

    @Test
    public void testRootGroupsByName() {
        registry.groupsOf(ROOT_NAME);
    }

    @Test
    public void testRootGroupsById() {
        registry.groupsOf(ROOT_ID);
    }
}
