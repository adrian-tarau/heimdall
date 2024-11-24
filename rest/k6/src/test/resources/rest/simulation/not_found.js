import http from 'k6/http';
import {sleep} from 'k6';

export const options = {
    vus : 1,
    iterations : 100,
    duration: '2m'
};

export default function () {
    http.get(`${__ENV.APP_URI}/not_found`);
    sleep(1 + 2 * Math.random());
}