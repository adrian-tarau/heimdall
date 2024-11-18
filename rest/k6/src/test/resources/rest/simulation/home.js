import http from 'k6/http';
import {sleep} from 'k6';

export const options = {
    vus: 2,
    duration: '5s',
};
export default function () {
    http.get(`${__ENV.REST_API_URI}`);
    sleep(0.5);
}