# Checkmate search for progressive chess

An A* based search algorithm with custom heuristics that finds a sequence of legal moves leading to checkmate for the given board state of a progressive chess game. A homework assignment for the Algorithms course at the University of Ljubljana, Faculty of Computer and Information Science in 2019. Porocilo.pdf file contains a full report in the Slovene language. 

[Progressive chess](https://en.wikipedia.org/wiki/Progressive_chess) is one of the chess variants in which every player makes one more move than the previous one and a checkmate is only valid if it occurs on the player's last move. 
A good heuristic function, that effectively guides the search, is essential when developing a search algorithm due to the combinatorial complexity of the problem.
During development, we relied on V. Janko's Master Thesis [1] and M. Guid's article [2] which focuses on this particular topic in more detail, as well as the ProgressiveChess.jar library. The code was written in Java with IntelliJ IDE. 

Our implementation takes a current board state in FEN notation and finds a path by searching through the nodes of the search space (in our case these are board states that result due to a specific move) using A* until it reaches the goal node (checkmate). Costs are estimated using an optimistic heuristic in an effort to find the shortest path. Through experimentation, we defined our heuristic as:

*0.1∗Manhattan - 0.9∗Covering - 0.8∗Depth - 0.5 if the move leads to a queen promotion of the peasant piece*.

Manhattan simply sums up all of the Manhattan distances from all of the player's pieces to the opponent's king. Covering is a number of free tiles around the opponent's king. Depth is the current path's depth (number of previous moves). With this heuristic, our algorithm solves 36 testing cases within the timeout limit of 20 seconds. On average it takes 7 seconds per solved case.

## Literature

[1] Janko, V., Guid, M.: Razvoj programa za igranje 1-2-3 šsaha: magistrsko delo. V. Janko (2015)

[2] Janko, V., Guid, M.: A program for progressive chess. Theoretical Computer Science 644, 76 – 91 (2016). https://doi.org/https://doi.org/10.1016/j.tcs.2016.06.028, http://www.sciencedirect.com/science/article/pii/S0304397516302730, recent Advances in Computer Games



