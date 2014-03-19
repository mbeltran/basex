package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import java.io.*;

import org.basex.data.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.io.serial.SerializerOptions.YesNo;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Fn:put operation primitive.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public final class Put extends DBUpdate {
  /** Target paths. The same node can be stored in multiple locations. */
  private final StringList paths = new StringList(1);
  /** Node id of the target node. Target nodes are identified via their ID, as structural
   *  changes (delete/insert) during the snapshot lead to PRE value shifts on disk.
   *  In addition, deleted/replaced nodes will not be serialized by fn:put as the
   *  identity of these nodes is gone - which is easier to track operating on IDs. */
  public final int nodeid;

  /**
   * Constructor.
   * @param id target node id
   * @param data target data reference
   * @param path target path
   * @param info input info
   */
  public Put(final int id, final Data data, final String path, final InputInfo info) {
    super(UpdateType.FNPUT, data, info);
    nodeid = id;
    paths.add(path);
  }

  @Override
  public void apply() throws QueryException {
    for(final String u : paths) {
      final int pre = data.pre(nodeid);
      if(pre == -1) return;
      final DBNode node = new DBNode(data, pre);
      try(final PrintOutput po = new PrintOutput(u)) {
        // try to reproduce non-chopped documents correctly
        final SerializerOptions pr = new SerializerOptions();
        pr.set(SerializerOptions.INDENT, node.data.meta.chop ? YesNo.YES : YesNo.NO);
        final Serializer ser = Serializer.get(po, pr);
        ser.serialize(node);
        ser.close();
      } catch(final IOException ex) {
        throw UPPUTERR.get(info, u);
      }
    }
  }

  @Override
  public void merge(final Update up) {
    for(final String path : ((Put) up).paths) paths.add(path);
  }

  @Override
  public int size() {
    return paths.size();
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + nodeid + ", " + paths.get(0) + ']';
  }

  @Override
  public void prepare(final MemData tmp) { }
}
