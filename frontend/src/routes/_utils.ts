export function groupBy<T>(list: Array<T>, keyGetter): Map<any, T> {
  const map = new Map();
  list.forEach((item) => {
    const key = keyGetter(item);
    const collection = map.get(key);
    if (!collection) {
      map.set(key, [item]);
    } else {
      collection.push(item);
    }
  });
  return map;
}


export async function delay(msec) {
  return new Promise(resolve => {  setTimeout(() => { resolve('') }, msec);})
}