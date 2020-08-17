import ru.avem.navitest.database.dot.ProtocolDot
import ru.avem.navitest.database.graph.ProtocolGraph

object Singleton {
    lateinit var currentProtocol: ProtocolGraph
    lateinit var currentProtocolDot: ProtocolDot
}
