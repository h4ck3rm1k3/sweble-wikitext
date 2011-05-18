# this uses peg-1.4 from http://piumarta.com/software/peg/peg-0.1.4/ for validating

all : mediawiki

mediawiki.c :mediawiki.peg ../../peg-0.1.4/peg
	../../peg-0.1.4/peg -v mediawiki.peg -omediawiki.c

mediawiki : mediawiki.c
	g++ mediawiki.c