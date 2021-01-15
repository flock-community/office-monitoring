import {useState} from "react";

let ITEMS_LIMIT = 100;
export const useAddToList = (itemsLimit = ITEMS_LIMIT) => {
    const [items, setWords] = useState([])

    const addToList = (itemToAdd) => {
        if (!itemToAdd) return
        setWords(prevState => {
            const newArray = [itemToAdd].concat(prevState)
            if (newArray.length > itemsLimit) {
                newArray.pop()
            }

            return newArray
        })
    }

    return [items, addToList]
}

const mapToRadialChartFormat = (distributionDTO) => {
    return Object.entries(distributionDTO.wordDistribution).map(([word, value]) => {
        return {angle: value, label: word, x:word, y:value}
    });
};

export const parseNewWordTrendDistribution = distributionDTO => {
    const mapToRadialChartFormat1 = mapToRadialChartFormat(distributionDTO);
    // console.log(mapToRadialChartFormat1);
    return {
        total: distributionDTO.wordTotal,
        distribution: mapToRadialChartFormat1
    }
};

export const emptyDistribution = {
    total: 0,
    distribution: [{
        angle: 0.1,
        label: 'Flock.',
        x: 'Flock.',
        y: 0.1
    }]
};