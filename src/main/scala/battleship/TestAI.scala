package battleship
import scala.util.Random
import scala.annotation.tailrec

object TestAI extends App {
    // ===== CONST
    val GAME_TYPES = Array(
        "AI(low) vs AI (medium)",
        "AI(low) vs AI (hard)",
        "AI(medium) vs AI (hard)",
    )

    val SHIPS = Array(
        new Ship("Carrier","C",5),
        new Ship("Battleship","B",4),
        new Ship("Cruiser","c",3),
        new Ship("Submarine","S",3),
        new Ship("Destroyer","D",2)
    )

    val NB_OF_GAMES_TO_PLAY = 100
    val output = ConsoleOutput

    start()

    /**
        Start the Battleship program.
    */
    def start(): Unit = {

        // Init
        output.clear()
        output.display("====================")
        output.display("==== BATTLESHIP ====")
        output.display("====================")
        val gameType = askForGameType()

        gameType match {
            // AI(low) vs AI (medium)
            case 0 => {
                output.clear()
                val beginner = (new Random).nextInt(2)
                // Place ships
                val player1: Player = this.getNewPlayerWithShipsPlaced(SHIPS, new AILow())   
                val player2: Player = this.getNewPlayerWithShipsPlaced(SHIPS, new AIMedium())   
                
                // Launch the battle
                val state = if (beginner == 0) {
                    new GameState(player1, player2, beginner)
                } else {
                    new GameState(player1, player2, beginner)
                }
                val lastState = gameLoop(state)
                output.clear()
                printFinalResult(lastState)
            }
            // AI(low) vs AI (hard)
            case 1 => {
                output.displayError("Game type not implementend yet.")
            }
            // AI(medium) vs AI (hard)
            case 2 => {
                output.displayError("Game type not implementend yet.")
            }
            case _ => {
                output.displayError("Unkown game type.")
            }
        }     
    }

    // Game loop (play another game ?)
    @tailrec
    def gameLoop(state: GameState): GameState = {
        if(state.nbOfGames != 0) {
            if(state.nbOfGames < NB_OF_GAMES_TO_PLAY) {  
                val p1 = state.player1.copyWithNewGrid(myGrid = new Grid()) 
                val p2 = state.player2.copyWithNewGrid(myGrid = new Grid()) 

                // Place ships
                val player1: Player = this.getNewPlayerWithShipsPlaced(SHIPS, p1).emptyShotsFired() 
                val player2: Player = this.getNewPlayerWithShipsPlaced(SHIPS, p2).emptyShotsFired()    

                // Launch the battle
                val newState = if (state.playerWhoBegins == 0) {
                    state.copy(player1 = player1, player2 = player2, currentPlayer = player2, playerWhoBegins = 1)
                } else {
                    state.copy(player1 = player1, player2 = player2, currentPlayer = player1, playerWhoBegins = 0)
                }

                gameLoop(battleLoop(newState))
            }
            else state
        } else {
            // launch the first game
            gameLoop(battleLoop(state))
        }
    }

    // Battle loop (turn after turn)
    @tailrec
    def battleLoop(state: GameState): GameState = {
        val nextPlayer = if(state.currentPlayer == state.player1) state.player2 else state.player1
        val currentPlayer = state.currentPlayer

        // Shoot
        val coords = currentPlayer.askForShootCoordinates(nextPlayer.myGrid)
        val shotResult = nextPlayer.myGrid.shootHere(coords._1, coords._2)
        val ship: Option[Ship] = shotResult._1
        val shipName = ship.getOrElse("")
        val shotState: String = shotResult._2
        val newGrid: Grid = shotResult._3

        val lastShot: (Int,Int,String) = (coords._1, coords._2, shotState)
        val newShotsFired = currentPlayer.shotsFired + lastShot
        
        // Update data by creating new objects 
        val nextPlayerWithGridUpdated = nextPlayer.copyWithNewGrid(myGrid = newGrid)
        val newCurrentPlayer = currentPlayer.copyWithNewShotsFired(shotsFired = newShotsFired)

        // Check if game is over
        if(nextPlayerWithGridUpdated.myGrid.areAllShipsSunk()) {

            // Increase score of currentPlayer
            val newCurrentPlayerUpdated = newCurrentPlayer.copyWithNewScore(score = newCurrentPlayer.score + 1).copyWithNewShotsFired(shotsFired = newShotsFired)
            
            // Player1 saved as player1 (same for p2)
            val lastState = if(state.currentPlayer == state.player1) 
                    state.copy(player1 = newCurrentPlayerUpdated, player2 = nextPlayerWithGridUpdated, nbOfGames = state.nbOfGames + 1)
                else state.copy(player1 = nextPlayerWithGridUpdated, player2 = newCurrentPlayerUpdated, nbOfGames = state.nbOfGames + 1)

            return lastState
        }

        // Prepare for next turn
        if(newCurrentPlayer == state.player1) 
            battleLoop(state.copy(player1 = newCurrentPlayer, player2 = nextPlayerWithGridUpdated, currentPlayer = nextPlayerWithGridUpdated))
        else 
            battleLoop(state.copy(player1 = nextPlayerWithGridUpdated, player2 = newCurrentPlayer, currentPlayer = nextPlayerWithGridUpdated))
    }

    /**
        Return a new Player with the list of ships placed on his grid.
    */
    @tailrec
    def getNewPlayerWithShipsPlaced(ships: Array[Ship], p: Player): Player = {  
        if(ships.length == 0) p
        else {
            val ship = ships.last
            val newPlayer = p.askToPlaceAShip(ship)
            getNewPlayerWithShipsPlaced(ships.take(ships.length - 1), newPlayer)
        }        
    }

    def printFinalResult(state: GameState): Unit = {
        output.display(state.nbOfGames + " game(s) played.")
        output.display("===== SCORE =====\n" 
            + state.player1.name + ": " + state.player1.score 
            + "\n" + state.player2.name + ": " + state.player2.score 
        )   
    }

    /**
        Ask the user the game type he wants: 
    */
    def askForGameType(): Int = {
        output.display("Choose your game type (integer only):")
        GAME_TYPES.zipWithIndex.foreach{ 
            case(x, i) => output.display(i + ". " + x)
        }
        val valueTyped = scala.io.StdIn.readLine()
        try {
            val intTyped = Integer.parseInt(valueTyped)
            if(intTyped < 0 || intTyped >= this.GAME_TYPES.length) askForGameType()
            else intTyped
        } catch {
            case e: NumberFormatException => askForGameType()
        }
    }
}