/*******************************************************************************
    Copyright (c) 2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

var x = 10;

if (Math.random()) f = function () {  x++; }
else f = function () {}
f();

x = 20;
