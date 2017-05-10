package com.fnee.pbn;/*
 * File   :     PbnTagUse.java
 * Author :     Tis Veugen
 * Date   :     1999-08-13
 * PBN    :     1.6
 *
 * History
 * -------
 */

public class PbnTagUse
{
  public static final int       NONE = 0;
  public static final int       USED = 1;
  public static final int       PREV = 2;   // Same as previous due to '#'
  public static final int       HSHS = 3;   // Tag starting with "##"
  public static final int       COPY = 4;   // Same as previous due to '##'
}
