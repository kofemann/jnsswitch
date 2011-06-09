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

/**
 * A class to access user entries on a Unix like systems.
 * The actual lookups happen according to systems {@literal /etc/nsswith.conf}
 * configuration.
 */
public class UserRegistry {

    /*
     * struct passwd equivalent as defined in <pwd.h>
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
     * struct group equivalent as defined in <pwd.h>
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

    /**
     * Get numeric id of the user with a given name.
     * @param name of the user
     * @return uid
     * @throws NoSuchElementException if user does not exists.
     */
    public int uidByName(String name) throws NoSuchElementException {
        __password pwrecord = _libc.getpwnam(name);
        if (pwrecord == null) {
            throw new NoSuchElementException("user " + name + " does not exists");
        }

        return pwrecord.uid;
    }

    /**
     * Get numeric id of the group with a given name.
     * @param name of the user
     * @return gid
     * @throws NoSuchElementException if group does not exists.
     */
    public int gidByName(String name) throws NoSuchElementException {
        __group group = _libc.getgrnam(name);
        if (group == null) {
            throw new NoSuchElementException("group " + name + " does not exists");
        }

        return group.gid;
    }

    /**
     * Get a name of the user with a given id;
     * @param id of the user
     * @return user name
     * @throws NoSuchElementException if user does not exists.
     */
    public String userById(int id) throws NoSuchElementException {
        __password pwrecord = _libc.getpwuid(id);
        if (pwrecord == null) {
            throw new NoSuchElementException("uid " + id + " does not exists");
        }

        return pwrecord.name;
    }

    /**
     * Get a name of the group with a given id;
     * @param id of the group
     * @return user name
     * @throws NoSuchElementException if group does not exists.
     */
    public String groupById(int id) throws NoSuchElementException {
        __group group = _libc.getgrgid(id);
        if (group == null) {
            throw new NoSuchElementException("gid " + id + " does not exists");
        }

        return group.name;
    }

    /**
     * Get array of group ids to which a user belongs. 
     * @param id of the user
     * @return array of gids.
     * @throws NoSuchElementException  if user does not exists.
     */
    public int[] groupsOf(int id) throws NoSuchElementException {

        __password pwrecord = _libc.getpwuid(id);
        if (pwrecord == null) {
            throw new NoSuchElementException("uid " + id + " does not exists");
        }
        return groupsOf(pwrecord);
    }

    /**
     * Get array of group ids to which a user belongs. 
     * @param name of the user
     * @return array of gids.
     * @throws NoSuchElementException  if user does not exists.
     */
    public int[] groupsOf(String name) throws NoSuchElementException {

        __password pwrecord = _libc.getpwnam(name);
        if (pwrecord == null) {
            throw new NoSuchElementException("user " + name + " does not exists");
        }

        return groupsOf(pwrecord);
    }

    private int[] groupsOf(__password pwrecord) {

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
}
