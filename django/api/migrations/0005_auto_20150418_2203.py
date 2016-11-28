# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0004_studysession_is_ta'),
    ]

    operations = [
        migrations.AlterField(
            model_name='studysession',
            name='is_ta',
            field=models.BooleanField(default=False),
            preserve_default=True,
        ),
    ]
