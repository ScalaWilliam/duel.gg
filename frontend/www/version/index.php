<?php
header("Content-Type: text/plain");
system("git log --pretty=format:'%h' -n 1");