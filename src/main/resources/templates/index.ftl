<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="${publicAt('css/bulma.min.css')}" rel="stylesheet">
        <script src="${publicAt('js/htmx.min.js')}" type="application/javascript"></script>
    </head>
    <body>
        <section class="section">
            <div class="container">
                <h1 class="title">
                    Syslog Director
                </h1>
                <p class="subtitle">
                    Receive and/or forward syslog messages &dash;
                    <small><a href="https://github.com/mnellemann/syslogd" target="_blank">https://github.com/mnellemann/syslogd</a></small>
                </p>
            </div>
        </section>
        <section class="section">
            <table class="table is-fullwidth is-striped" hx-get="/log" hx-trigger="load, every 2s" hx-swap="innerHTML">
            </table>
        </section>
    </body>
</html>