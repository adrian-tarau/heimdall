import http from 'k6/http';
import {sleep} from 'k6';

export const options = {
    duration: '2m',
    stages: [
        {duration: '30s', target: 1},
        {duration: '30s', target: 5},
        {duration: '20s', target: 0},
    ]
};
export default function () {
    http.get(`${__ENV.APP_URI}`);
    sleep(1 + 2 * Math.random());
}