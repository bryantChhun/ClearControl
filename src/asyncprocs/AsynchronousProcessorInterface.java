package asyncprocs;

import java.io.Closeable;

public interface AsynchronousProcessorInterface<I, O> extends
																											ProcessorInterface<I, O>,
																											Closeable
{

	public void connectToReceiver(AsynchronousProcessorInterface<O, ?> pAsynchronousProcessor);

	public boolean start();

	public boolean stop();

	public boolean passOrWait(I pObject);

	public boolean passOrFail(I pObject);

}
