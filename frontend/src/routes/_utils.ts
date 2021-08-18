const delay = async milliSeconds => new Promise(resolve => {
  setTimeout(() => {
    resolve('')
  }, milliSeconds);
});

enum UpdateStatus {
  IDLE= "IDLE",
  UPDATING = "UPDATING",
  QUEUED = "QUEUED",
}

export const debounceMaxInterval = (block: (...args) => void, interval: number) => {
  let _updating: UpdateStatus = UpdateStatus.IDLE;
  let _args: any[] = [];

  const debounceInternal = async (...args) => {
    if (_updating !== UpdateStatus.IDLE) {
      _args = args;
      _updating = UpdateStatus.QUEUED;
      return;
    }

    _updating = UpdateStatus.UPDATING;
    block.apply(this, args);

    await delay(interval);
    // @ts-ignore
    if (_updating === UpdateStatus.QUEUED) {
      _updating = UpdateStatus.IDLE;
      await debounceInternal(..._args);
    } else {
      _updating = UpdateStatus.IDLE;
    }
  };

  return (...args) => debounceInternal(...args);
};

