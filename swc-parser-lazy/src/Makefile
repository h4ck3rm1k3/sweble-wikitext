# this uses peg-1.4 from http://piumarta.com/software/peg/peg-0.1.4/ for validating

all : mediawiki

mediawiki.c :mediawiki.peg
	~/experiments/sweble-others/peg-0.1.4/peg -v mediawiki.peg -o  mediawiki.c

mediawiki : mediawiki.c
	g++ mediawiki.c