import http from 'k6/http'
import {group, sleep} from 'k6';

export const options = {
    vus: 2,
    duration: '10s',
};

export default function () {
    group('visit product listing page', function () {
        http.get('https://test.k6.io/');
        sleep(0.05);
    });
    group('add several products to the shopping cart', function () {
        http.get('https://test.k6.io/');
        sleep(0.1);
    });
    group('visit login page', function () {
        http.get('https://test.k6.io/');
        sleep(0.15);
    });
    group('authenticate', function () {
        http.get('https://test.k6.io/');
        sleep(0.2);
    });
    group('checkout process', function () {
        http.get('https://test.k6.io/');
        sleep(0.25);
    });
}