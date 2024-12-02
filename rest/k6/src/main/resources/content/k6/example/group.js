import {group} from 'k6';

export default function () {
    group('visit product listing page', function () {
        http.get('https://test.k6.io/');
    });
    group('add several products to the shopping cart', function () {
        http.get('https://test.k6.io/');
    });
    group('visit login page', function () {
        http.get('https://test.k6.io/');
    });
    group('authenticate', function () {
        http.get('https://test.k6.io/');
    });
    group('checkout process', function () {
        http.get('https://test.k6.io/');
    });
}