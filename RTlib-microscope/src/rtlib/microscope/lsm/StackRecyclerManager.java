package rtlib.microscope.lsm;

import java.util.concurrent.ConcurrentHashMap;

import coremem.recycling.BasicRecycler;
import coremem.recycling.RecyclerInterface;
import net.imglib2.img.basictypeaccess.offheap.ShortOffHeapAccess;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import rtlib.stack.ContiguousOffHeapPlanarStackFactory;
import rtlib.stack.StackInterface;
import rtlib.stack.StackRequest;

public class StackRecyclerManager implements AutoCloseable
{

	final private ContiguousOffHeapPlanarStackFactory<UnsignedShortType, ShortOffHeapAccess> mOffHeapPlanarStackFactory = new ContiguousOffHeapPlanarStackFactory<>();

	final private ConcurrentHashMap<String, RecyclerInterface<StackInterface<UnsignedShortType, ShortOffHeapAccess>, StackRequest<UnsignedShortType>>> mRecyclerMap = new ConcurrentHashMap<>();

	public void close()
	{
		mRecyclerMap.clear();
	}

	public RecyclerInterface<StackInterface<UnsignedShortType, ShortOffHeapAccess>, StackRequest<UnsignedShortType>> getRecycler(	String pName,
																																int pMaximumNumberOfAvailableObjects,
																																int pMaximumNumberOfLiveObjects)
	{

		RecyclerInterface<StackInterface<UnsignedShortType, ShortOffHeapAccess>, StackRequest<UnsignedShortType>> lRecycler = mRecyclerMap.get(pName);

		if (lRecycler == null)
		{

			lRecycler = new BasicRecycler<>(mOffHeapPlanarStackFactory,
											pMaximumNumberOfAvailableObjects,
											pMaximumNumberOfLiveObjects,
											true);
			mRecyclerMap.put(pName, lRecycler);
		}

		return lRecycler;

	}

}
