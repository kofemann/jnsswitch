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

package org.kofemann.jnsswitch;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import java.util.NoSuchElementException;

public class UserRegistry {

    /*
     * as defined in <pwd.h>
     */
    static public class __password extends Structure {

        public String name;
        public String passwd;
        public int uid;
        public int gid;
        public String gecos;
        public String dir;
        public String shell;
    }

    /*
     * as defined in <pwd.h>
     */
    static public class __group extends Structure {

        public String name;
        public String passwd;
        public int gid;
        public Pointer mem;
    }

    /*
     * hook some functions from libc
     */
    public interface LibC extends Library {

        __password getpwnam(String name);

        __password getpwuid(int id);

        __group getgrnam(String name);

        __group getgrgid(int id);

        int getgrouplist(String user, int gid, int[] groups, IntByReference ngroups);
    }

    /**
     * handle to libc.
     */
    private final LibC _libc = (LibC) Native.loadLibrary("c", LibC.class);

    public int uidByName(String name) throws NoSuchElementException {
        __password pwrecord = _libc.getpwnam(name);
        if (pwrecord == null) {
            throw new NoSuchElementException("user " + name + " does not exists");
        }

        return pwrecord.uid;
    }

    public int gidByName(String name) throws NoSuchElementException {
        __group group = _libc.getgrnam(name);
        if (group == null) {
            throw new NoSuchElementException("group " + name + " does not exists");
        }

        return group.gid;
    }

    public String userById(int id) throws NoSuchElementException {
        __password pwrecord = _libc.getpwuid(id);
        if (pwrecord == null) {
            throw new NoSuchElementException("uid " + id + " does not exists");
        }

        return pwrecord.name;
    }

    public String groupById(int id) throws NoSuchElementException {
        __group group = _libc.getgrgid(id);
        if (group == null) {
            throw new NoSuchElementException("gid " + id + " does not exists");
        }

        return group.name;
    }

    private int[] groupsOf(__password pwrecord) throws NoSuchElementException {

        boolean done = false;
        int[] groups = new int[0];
        while (!done) {
            IntByReference ngroups = new IntByReference();
            ngroups.setValue(groups.length);
            if (_libc.getgrouplist(pwrecord.name, pwrecord.gid, groups, ngroups) < 0) {
                groups = new int[ngroups.getValue()];
                continue;
            }
            done = true;
        }

        return groups;
    }

    public int[] groupsOf(int id) throws NoSuchElementException {

        __password pwrecord = _libc.getpwuid(id);
        if (pwrecord == null) {
            throw new NoSuchElementException("uid " + id + " does not exists");
        }
        return groupsOf(pwrecord);
    }

    public int[] groupsOf(String name) throws NoSuchElementException {

        __password pwrecord = _libc.getpwnam(name);
        if (pwrecord == null) {
            throw new NoSuchElementException("user " + name + " does not exists");
        }

        return groupsOf(pwrecord);
    }
}
