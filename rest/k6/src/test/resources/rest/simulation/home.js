import http from 'k6/http';
import {sleep} from 'k6';

export const options = {
    vus: 1,
    iterations: 50,
    duration: '2m',
    stages: [
        {duration: '30s', target: 1},
        {duration: '30s', target: 5},
        {duration: '20s', target: 0},
    ]
};
export default function () {
    http.get(`${__ENV.REST_API_URI}`);
    sleep(1 + 2 * Math.random());
}