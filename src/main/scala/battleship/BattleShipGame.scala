package battleship
import scala.util.Random

object BattleSchipGame extends App {
    // ===== CONST
    val GAME_TYPES = Array(
        "Human vs Human",
        "Human vs IA low",
        "Human vs IA medium",
        "Human vs IA hard",
        "IA low vs IA medium",
        "IA medium vs IA hard",
        "IA low vs IA hard"
    )

    val SHIPS = Array(
        new Ship("Carrier","C",5),
        /*new Ship("Battleship","B",4),
        new Ship("Cruiser","c",3),
        new Ship("Submarine","S",3),
        new Ship("Destroyer","D",2),*/
    )

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

        // TODO : case depending on gameType
        output.clear()
        output.display("Player 1 name:")
        val p1Name = scala.io.StdIn.readLine()
        val p1 = new Human(p1Name)
        output.display("Player 2 name:")
        val p2Name = scala.io.StdIn.readLine()
        val p2 = new Human(p2Name)

        val beginner = (new Random).nextInt(2)

        // Place ships
        val player1: Player = this.getNewPlayerWithShipsPlaced(SHIPS, p1)
        output.clear()
        output.display(player1.myGrid.toStringToSelf())
        output.display("Press any key to let " + p2Name + " place his ships.")
        scala.io.StdIn.readLine()
        output.clear()

        val player2: Player = this.getNewPlayerWithShipsPlaced(SHIPS, p2)   
        output.clear()
        output.display(player2.myGrid.toStringToSelf())
        output.display("Press any key to start the battle.")
        scala.io.StdIn.readLine()
        output.clear()
        
        // Launch the battle
        val state = if (beginner == 0) {
            output.display(player1.name + " starts.") 
            new GameState(new Human(name = player1.name, myGrid = player1.myGrid), new Human(name = player2.name, myGrid = player2.myGrid), beginner)
        } else {
            output.display(player2.name + " starts.")
            new GameState(new Human(name = player1.name, myGrid = player1.myGrid), new Human(name = player2.name, myGrid = player2.myGrid), beginner)
        }
        val lastState = gameLoop(state)
    }

    // Game loop (turn after turn)
    def gameLoop(state: GameState): GameState = {
        val nextPlayer = if(state.currentPlayer == state.player1) state.player2 else state.player1
        val currentPlayer = state.currentPlayer

        // Shoot
        output.display(currentPlayer.name + ", it's your turn!")

        val coords = currentPlayer.askForShootCoordinates(nextPlayer.myGrid)

        val shotResult = nextPlayer.myGrid.shootHere(coords._1, coords._2)
        val ship: Option[Ship] = shotResult._1
        val shipName = ship.getOrElse("")
        val shotState: String = shotResult._2
        val newGrid: Grid = shotResult._3

        if(shotState == Grid.MISS) output.display("You missed!")
        if(shotState == Grid.HIT) output.display("You hit a " + shipName + "!")
        if(shotState == Grid.SUNK) output.display("You have sunk a " + shipName + "!!")
        
        // Update data by creating new objects 
        val nextPlayerWithGridUpdated = if(nextPlayer.isInstanceOf[Human]) nextPlayer.asInstanceOf[Human].copy(myGrid = newGrid) 
            else nextPlayer.asInstanceOf[IA].copy(myGrid = newGrid)
        
        output.display(nextPlayerWithGridUpdated.myGrid.toStringToOpponent())

        // Check if game is over
        if(nextPlayerWithGridUpdated.myGrid.areAllShipsSunk()) {
            output.display("All " + nextPlayer.name + "'s ships sunk, " + currentPlayer.name + " wins ! Congratulations!")
            return state
        }

        // Prepare for next turn
        output.display("Press any key to let " + nextPlayer.name + " plays.")
        scala.io.StdIn.readLine()
        output.clear()    
        if(currentPlayer == state.player1) 
            gameLoop(state.copy(player2 = nextPlayerWithGridUpdated, currentPlayer = nextPlayerWithGridUpdated))
        else 
            gameLoop(state.copy(player1 = nextPlayerWithGridUpdated, currentPlayer = nextPlayerWithGridUpdated))
    }

    /**
        Return a new Player with the list of ships placed on his grid.
    */
    def getNewPlayerWithShipsPlaced(ships: Array[Ship], p: Player): Player = {  
        if(ships.length == 0) p
        else {
            val ship = ships.last
            output.clear()
            output.display(p.name + " place the ship: " + ship.toString)
            val newPlayer = p.askToPlaceAShip(ship)
            getNewPlayerWithShipsPlaced(ships.take(ships.length - 1), newPlayer)
        }        
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